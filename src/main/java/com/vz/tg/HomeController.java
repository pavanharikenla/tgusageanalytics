package com.vz.tg;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.vz.tg.model.HomeBean;
import com.vz.tg.services.HomeService;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	@Autowired
	HomeService homeservice ;
	private static HashMap<String,String> timerMap= new HashMap<String,String>();
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView login(Locale locale, Model model) {
		ModelAndView mav = new ModelAndView("login", "model", "");
		
		timerMap.put("00", "12AM");
		timerMap.put("01", "01AM");
		timerMap.put("02", "02AM");
		timerMap.put("03", "03AM");
		timerMap.put("04", "04AM");
		timerMap.put("05", "05AM");
		timerMap.put("06", "06AM");
		timerMap.put("07", "07AM");
		timerMap.put("08", "08AM");
		timerMap.put("09", "09AM");
		timerMap.put("10", "10AM");
		timerMap.put("11", "11AM");
		timerMap.put("12", "12PM");
		
		timerMap.put("13", "01PM");
		timerMap.put("14", "02PM");
		timerMap.put("15", "03PM");
		timerMap.put("16", "04PM");
		timerMap.put("17", "05PM");
		timerMap.put("18", "06PM");
		timerMap.put("19", "07PM");
		timerMap.put("20", "08PM");
		timerMap.put("21", "09PM");
		timerMap.put("22", "10PM");
		timerMap.put("23", "11PM");
		
		logger.info("bye now");
		return mav;
	}
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public ModelAndView home(Locale locale, Model model,HttpServletRequest request, @RequestParam("userMobile") String mobileNum) {
		
		//logger.info("Welcome home! The client locale is {}.", locale);
		
		
		//model = new object.. set values and set to MAV..
		String mobNumber = mobileNum;//"9052100567";//read from login..
		if(mobNumber!=null && !"".equalsIgnoreCase(mobNumber)){
			request.getSession().setAttribute("userMobile", mobNumber);	
		}else{
			mobNumber = (String)request.getSession().getAttribute("userMobile");
		}
		
		
		DateFormat solrFormat = new SimpleDateFormat ("yyyy-MM-dd'T'hh:mm:ss'Z'");
		DateFormat dateFormat = new SimpleDateFormat ("E MMM d hh:mm:ss zzz yyyy");
		SimpleDateFormat newFormat = new SimpleDateFormat("MM-dd-yyyy");
		
		DateFormat dateFormatsolr = new SimpleDateFormat("yyyy-MM-dd");

		int pastdayscount = 7;
		
		SolrQuery query = null;
		QueryResponse response =null;
		
		HomeBean bean = new HomeBean();
		Collection<JSONObject> dataList = new ArrayList<JSONObject>();
		TreeMap<String,Long> dataListObj = new TreeMap<String,Long>();
		try{
			while(pastdayscount>0){
				Calendar cal = Calendar.getInstance();
		        cal.add(Calendar.DATE, -pastdayscount);
		        Date todate1 = cal.getTime();    
		        String fromdate = dateFormatsolr.format(todate1);
		        query = new SolrQuery("mobile:"+mobNumber+" AND data_usage_end_time:["+fromdate+"T00:00:00Z TO "+fromdate+"T23:59:59Z] AND dataUsed:[1 TO *]");
		        query.setRows(0);
		        query.addFacetField("dataUsed");
		        response = homeservice.getServiceResponse(query);
		        
		        FacetField dataUsedFacetList = response.getFacetField("dataUsed");
				List<Count> dataValues = dataUsedFacetList.getValues();
				float dataTotal =0;
				for(Count dataEntry:dataValues){
					dataTotal = dataTotal + Float.parseFloat(dataEntry.getName()) *dataEntry.getCount();
				}
		        
				String userFormatDate = newFormat.format(todate1);
				
				JSONObject timeObject = new JSONObject();
				timeObject.put("date", userFormatDate);
				timeObject.put("bytes", dataTotal*1024);
				dataList.add(timeObject);
				dataListObj.put(userFormatDate, (long)dataTotal*1024);
				
		        
		        
		        
				pastdayscount = pastdayscount-1;
			}
		
        
		
			
			JSONObject finalObj = new JSONObject();
			finalObj.put("dataListByDate", dataList);
			bean.setDataListRecords(dataListObj);
			bean.setDataUsageList(finalObj);
			
			//usage by application...
			Collection<JSONObject> appList = new ArrayList<JSONObject>();
			query = new SolrQuery("mobile:"+mobNumber+" AND data_usage_end_time:[NOW-6DAY TO NOW]");
			query.setFacet(true);
			
			query.addFacetPivotField("mobile_app_name,dataUsed");
			ConcurrentHashMap<String, Integer> appUsageMap = new ConcurrentHashMap<String, Integer>();
			try {

				response = homeservice.getServiceResponse(query);

				if (response != null) {
					NamedList<List<PivotField>> pivotNamedList = response.getFacetPivot();

					System.out.println(pivotNamedList.size());
					for (int i = 0; i < pivotNamedList.size(); i++) {
						List<PivotField> pvotList = pivotNamedList.getVal(i);
						for (PivotField pf : pvotList) {
							if (pf != null) {
								System.out.println("pivot:" + pf.getValue());

								String appName = pf.getValue().toString();

								List<PivotField> dataUsedpivotList = pf.getPivot();
								float usedTotal = 0;
								for (PivotField dataUsedpivot : dataUsedpivotList) {
									if (dataUsedpivot != null) {
										if (dataUsedpivot.getValue() != null) {
											usedTotal = usedTotal + (Float)dataUsedpivot.getValue()*dataUsedpivot.getCount();
										}

									}
								}
								if (appUsageMap.containsKey(appName)) {
									int currentTotal = appUsageMap.get(appName);
									currentTotal = currentTotal + (int)usedTotal;
									appUsageMap.put(appName, currentTotal);
								} else {
									appUsageMap.put(appName, (int)usedTotal);
								}
							}
						}
					}
				}

			} catch (Exception e) {
				logger.error("exception in solr query:"+e.getMessage());
			}
			for (Map.Entry<String, Integer> e : appUsageMap.entrySet()) {
			    String appNameFinal = e.getKey();
			    //System.out.println(key);
			    Integer usedValFinal = e.getValue();
			    //System.out.println(value);
			    JSONObject applicaationObj = new JSONObject();
				applicaationObj.put("label", appNameFinal);
				applicaationObj.put("value", usedValFinal*1024);
				
				appList.add(applicaationObj);
			}
			
			
			/*JSONObject applicaationObj = new JSONObject();
			applicaationObj.put("label", "whatsapp");
			applicaationObj.put("value", 1435262);
			appList.add(applicaationObj);*/
			
			JSONObject appUsageObj = new JSONObject();
			appUsageObj.put("appUsageList", appList);
			bean.setAppUsageList(appUsageObj);
			
			
			
			
			
			
			/*QueryResponse response = homeservice.getServiceResponse(query);
			if(response!=null){
				 SolrDocumentList responseList = response.getResults();
				 long totalCount = responseList.getNumFound();
				 if(totalCount>0){
					 bean.setResultCount(totalCount);
				 }
				 List<GroupCommand> list = response.getGroupResponse().getValues();
				 GroupCommand groupList = list.get(0);
				 long totalCount = groupList.getMatches();
				 bean.setResultCount(totalCount);
				 List<Group> groups = groupList.getValues();
				 
				 long positivaTotal =0;
				 long neutralTotal =0;
				 if(groups!=null && groups.size()>0){
					 for(Group grp:groups){
						 String grpValue = grp.getGroupValue();
						 SolrDocumentList docList = grp.getResult();
						 long numFound = docList.getNumFound();
						 if(grpValue!=null){
							 int sentiment_val =  Integer.parseInt(grpValue);
							 switch(sentiment_val){
							 	case 0://Negative Needs immediate Attention....
							 		bean.setNegativeCount(numFound);
							 		break;
							 	case 1:
							 		neutralTotal = neutralTotal+numFound;
							 		break;
							 	case 2:
							 		//bean.setNeutralCount(numFound);
							 		neutralTotal = neutralTotal+numFound;
							 		break;
							 	case 3:
							 		positivaTotal = positivaTotal+numFound;
							 		break;
							 	case 4:
							 		positivaTotal = positivaTotal+numFound;
							 		break;
							 }
						 }
					 }
				 }
				 bean.setPositiveCount(positivaTotal);
				 bean.setNeutralCount(neutralTotal);
			}
			
			query.set("group", true);
			query.set("group.field", "agegroup");
			query.addSort("agegroup",SolrQuery.ORDER.asc);
			response = homeservice.getServiceResponse(query);
			if(response!=null){
				List<GroupCommand> list = response.getGroupResponse().getValues();
				 GroupCommand groupList = list.get(0);
				 //long totalCount = groupList.getMatches();
				 //bean.setResultCount(totalCount);
				 List<Group> groups = groupList.getValues();
				 Collection<JSONObject> jsonList = new ArrayList<JSONObject>();
				 if(groups!=null && groups.size()>0){
					 for(Group grp:groups){
						 String grpValue = grp.getGroupValue();
						 SolrDocumentList docList = grp.getResult();
						 long numFound = docList.getNumFound();
						 if(grpValue!=null && !"".equalsIgnoreCase(grpValue.trim())){
							 JSONObject json = new JSONObject();
							 json.put("label", grpValue);
							 json.put("value", numFound);
							 jsonList.add(json);
						 }
					 }
				 }
				 JSONObject finalObj = new JSONObject();
				 finalObj.put("agegroup", jsonList);
				 bean.setAgeGroup(finalObj);
			}
			
			DateFormat dateFormat = new SimpleDateFormat ("E MMM d hh:mm:ss zzz yyyy");
			SimpleDateFormat newFormat = new SimpleDateFormat("MM-dd-yyyy");
			query.addFacetPivotField("tweetPostedTime,tweet_category");
			query.addSort("tweetPostedTime",SolrQuery.ORDER.asc);
			query.setRows(0);
			JSONObject finalObj = new JSONObject();
			response = homeservice.getServiceResponse(query);
			List<Date> myList = new ArrayList<Date>();
			JSONObject tempObj = new JSONObject();
			Collection<JSONObject> categoryLst = new ArrayList<JSONObject>();
			if(response!=null){
				System.out.println(response);
				NamedList<List<PivotField>> pivotNamedList = response.getFacetPivot();
				System.out.println(pivotNamedList.size());
				for( int i=0;i<pivotNamedList.size();i++){
					List<PivotField> pvotList = pivotNamedList.getVal(i);
					for(PivotField pf:pvotList){
						if(pf!=null){
							System.out.println(pf.getValue());
							Date beforeParse = dateFormat.parse(pf.getValue().toString());
							myList.add(beforeParse);
							JSONObject categoryObj = new JSONObject();
							categoryObj.put("period", newFormat.format(beforeParse));
							//System.out.println(pf.getField());
							List<PivotField> categoryList = pf.getPivot();
							int smartPhoneTotal =0;
							int tabletsTotal =0;
							int productsTotal =0;
							for(PivotField categorypivot:categoryList){
								if(categorypivot!=null){
									if(categorypivot.getValue()!=null){
										String fullCatName = categorypivot.getValue().toString();
										if(fullCatName.contains("Smartphones")){
											smartPhoneTotal = smartPhoneTotal +categorypivot.getCount();
										}else if(fullCatName.contains("Tablets")){
											tabletsTotal = tabletsTotal +categorypivot.getCount();
										}else if(fullCatName.contains("Products")){
											productsTotal = productsTotal +categorypivot.getCount();
										}
									}
									
								}
							}
							categoryObj.put("Smartphones", smartPhoneTotal);
							categoryObj.put("Tablets", tabletsTotal);
							categoryObj.put("Products", productsTotal);
							tempObj.put(newFormat.format(beforeParse), categoryObj);
							//categoryLst.add(categoryObj);
						}
						
					}
					
				}
				//finalObj.put("categoryData", categoryLst);
			}
			
			Collections.sort(myList);
			Iterator<Date> itr = myList.iterator();
			while(itr.hasNext()){
				
				Date inputDate = dateFormat.parse(itr.next().toString());
				
				String tempKey = newFormat.format(inputDate);
				System.out.println(tempKey);
				JSONObject newObj = (JSONObject)tempObj.get(tempKey);
				categoryLst.add(newObj);
				
			}
			finalObj.put("categoryData", categoryLst);
			bean.setCategoryData(finalObj);
			
			String tweetType="*";
			Collection<JSONObject> tweetCollection = getTweetResponse(tweetType,0,10);
			bean.setEndRecord(10);
			if(tweetCollection!=null){
				LinkedHashMap<String, String> tweetList = new LinkedHashMap<String, String>();
				for(JSONObject tweet:tweetCollection){
					if(tweet!=null){
						tweetList.put((String)tweet.get("id"), (String)tweet.get("tweet"));
					}
				}
				bean.setTweetList(tweetList);
			}*/
			
			if(mobNumber!=null && !"".equalsIgnoreCase(mobNumber)){
				Thread threadNew = new Thread(new HourlyThread(mobNumber));
				threadNew.start();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		ModelAndView mav = new ModelAndView("home", "model", bean);
		logger.info("bye now");
		return mav;
	}
		
	@RequestMapping(value = "/getDataByDate", method = RequestMethod.GET)
	@ResponseBody
	public String getDataByDate(HttpServletRequest request, @RequestParam("dateVal") String dateVal) {
		JSONObject finalRes = new JSONObject();
		Collection<JSONObject> hourlyList = new ArrayList<JSONObject>();
		String mobNumber = (String) request.getSession().getAttribute("userMobile");
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
		SimpleDateFormat solrFormat = new SimpleDateFormat("yyyy-MM-dd");
		if (dateVal != null && !"".equalsIgnoreCase(dateVal)) {
			String[] dayHours = { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13",
					"14", "15", "16", "17", "18", "19", "20", "21", "22", "23" };

			Date newDate;
			String finalDate = "";
			try {
				newDate = sdf.parse(dateVal);
				finalDate = solrFormat.format(newDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (HourlyThread.hourlyListMap != null && HourlyThread.hourlyListMap.containsKey(finalDate)) {
				hourlyList = HourlyThread.hourlyListMap.get(finalDate);
			} else {
				for (int i = 0; i <= dayHours.length - 1; i++) {
					SolrQuery query = new SolrQuery(
							"mobile:" + mobNumber + " AND data_usage_end_time:[" + finalDate + "T" + dayHours[i]
									+ ":00:00Z TO " + finalDate + "T" + dayHours[i] + ":59:59Z] AND dataUsed:[1 TO *]");
					query.setRows(0);
					query.addFacetField("dataUsed");
					QueryResponse response = homeservice.getServiceResponse(query);
					if (response != null) {
						FacetField dataUsedFacetList = response.getFacetField("dataUsed");
						List<Count> dataValues = dataUsedFacetList.getValues();
						float dataTotal = 0;
						for (Count dataEntry : dataValues) {
							if (dataEntry.getCount() != 0) {
								dataTotal = dataTotal + Float.parseFloat(dataEntry.getName()) * dataEntry.getCount();
							} else {
								break;
							}
						}

						// String userFormatDate = newFormat.format(todate1);

						JSONObject timeObject = new JSONObject();

						timeObject.put("time", timerMap.get(dayHours[i]));
						timeObject.put("bytes", dataTotal * 1024);
						hourlyList.add(timeObject);
					}
				}
				// dataListObj.put(userFormatDate, (long)dataTotal*1024);

			}

		}
		
		/*JSONObject timeObj = new JSONObject();
		timeObj.put("time", "12AM");
		timeObj.put("bytes", "123456");
		hourlyList.add(timeObj);
		
		JSONObject timeObj1 = new JSONObject();
		timeObj1.put("time", "1AM");
		timeObj1.put("bytes", "12356");
		hourlyList.add(timeObj1);

		JSONObject timeObj2 = new JSONObject();
		timeObj2.put("time", "2AM");
		timeObj2.put("bytes", "23456");
		hourlyList.add(timeObj2);
		
		JSONObject timeObj3 = new JSONObject();
		timeObj3.put("time", "3AM");
		timeObj3.put("bytes", "12356");
		hourlyList.add(timeObj3);
		
		JSONObject timeObj4 = new JSONObject();
		timeObj4.put("time", "4AM");
		timeObj4.put("bytes", "3456");
		hourlyList.add(timeObj4);
		
		JSONObject timeObj5 = new JSONObject();
		timeObj5.put("time", "5AM");
		timeObj5.put("bytes", "1111");
		hourlyList.add(timeObj5);
		
		JSONObject timeObj6 = new JSONObject();
		timeObj6.put("time", "6AM");
		timeObj6.put("bytes", "12345");
		hourlyList.add(timeObj6);
		
		JSONObject timeObj7 = new JSONObject();
		timeObj7.put("time", "7AM");
		timeObj7.put("bytes", "123456");
		hourlyList.add(timeObj7);
		
		JSONObject timeObj8 = new JSONObject();
		timeObj8.put("time", "8AM");
		timeObj8.put("bytes", "1256");
		hourlyList.add(timeObj8);
		
		JSONObject timeObj9 = new JSONObject();
		timeObj9.put("time", "9AM");
		timeObj9.put("bytes", "123456");
		hourlyList.add(timeObj9);
		
		JSONObject timeObj10 = new JSONObject();
		timeObj10.put("time", "10AM");
		timeObj10.put("bytes", "1456");
		hourlyList.add(timeObj10);
		
		JSONObject timeObj11 = new JSONObject();
		timeObj11.put("time", "11AM");
		timeObj11.put("bytes", "12456");
		hourlyList.add(timeObj11);*/
		
		finalRes.put("hourlyListByDate", hourlyList);
		
		return finalRes.toString();
	}
	
	@RequestMapping(value = "/getPastDataByHours", method = RequestMethod.GET)
	@ResponseBody
	public String getPastDataByHours(@RequestParam("days") String dayCount) {
		JSONObject finalRes = new JSONObject();
		Collection<JSONObject> hourlyList = new ArrayList<JSONObject>();
		SimpleDateFormat solrFormat = new SimpleDateFormat("yyyy-MM-dd");
		String[] dayHours = { "12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM", "8AM", "9AM", "10AM", "11AM", "12PM", "01PM",
				"02PM", "03PM", "04PM", "05PM", "06PM", "07PM", "08PM", "09PM", "10PM", "11PM" };



		for (int i = 0; i <= dayHours.length - 1; i++) {
			if(HourlyThread.mapByHourly.containsKey(dayHours[i])){
				ConcurrentHashMap<String,Long>  dateValueMap = HourlyThread.mapByHourly.get(dayHours[i]);
				if(dateValueMap!=null && dateValueMap.size()>0){
					JSONObject timeObj = new JSONObject();
					timeObj.put("time", dayHours[i]);
					Iterator entries = dateValueMap.entrySet().iterator();
					while (entries.hasNext()) {
						Map.Entry entry = (Map.Entry)entries.next();
						String datev = (String)entry.getKey();
						Long actV = (Long)entry.getValue();
						timeObj.put(datev, actV);
					}
					hourlyList.add(timeObj);
				}
			}
		}
		
		/*JSONObject timeObj = new JSONObject();
		timeObj.put("time", "12AM");
		timeObj.put("24-10-2015", "123456");
		timeObj.put("25-10-2015", "123134");
		timeObj.put("26-10-2015", "345623");
		hourlyList.add(timeObj);
		
		JSONObject timeObj1 = new JSONObject();
		timeObj1.put("time", "1AM");
		timeObj1.put("24-10-2015", "123134");
		timeObj1.put("25-10-2015", "1231346");
		timeObj1.put("26-10-2015", "134");
		hourlyList.add(timeObj1);

		JSONObject timeObj2 = new JSONObject();
		timeObj2.put("time", "2AM");
		timeObj2.put("24-10-2015", "845612");
		timeObj2.put("25-10-2015", "2123456");
		timeObj2.put("26-10-2015", "634256");
		hourlyList.add(timeObj2);
		
		JSONObject timeObj3 = new JSONObject();
		timeObj3.put("time", "3AM");
		timeObj3.put("24-10-2015", "12356");
		timeObj3.put("25-10-2015", "121356");
		timeObj3.put("26-10-2015", "2356");
		hourlyList.add(timeObj3);*/
		
		finalRes.put("hourlyListByDays", hourlyList);
		// finalRes.put("hourlyListByDays", hourlyListStr);
		
		return finalRes.toString();
	}
	
	public Collection<JSONObject> getTweetResponse(String tweetType,int startRow,int endRow){
		SolrQuery query = new SolrQuery("sentimentScore:"+tweetType);
		query.setStart(startRow);
		query.setRows(endRow);
		HomeBean bean = new HomeBean();
		Collection<JSONObject> objectList = new ArrayList<JSONObject>();
		try{
		QueryResponse response = homeservice.getServiceResponse(query);
		if(response!=null){
			 SolrDocumentList responseList = response.getResults();
			 long totalCount = responseList.getNumFound();
			 if(totalCount>0){
				 bean.setResultCount(totalCount);
			 }
			  
			 for(SolrDocument document:responseList){
				 JSONObject json = new JSONObject();
				 json.put("id", document.getFieldValue("id"));
				 json.put("tweet", document.getFieldValue("tweet_content"));
				 objectList.add(json);
			 }
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return objectList;
	}
}
