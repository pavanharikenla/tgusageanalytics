package com.vz.tg;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

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

import com.vz.tg.model.ChartBean;
import com.vz.tg.model.HomeBean;
import com.vz.tg.services.HomeService;
import com.vz.tg.util.RandonGen;

/**
 * Handles requests for the application home page.
 */
@Controller
public class ChartController {
	
	private static final Logger logger = LoggerFactory.getLogger(ChartController.class);
	@Autowired
	HomeService homeservice ;
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/charts", method = RequestMethod.GET)
	public ModelAndView home(Locale locale, Model model,HttpServletRequest request) {
		
		logger.info("Welcome Charts! The client locale is {}.", locale);
		
		String mobNumber = "";//"9052100567";//read from login..
		mobNumber = (String)request.getSession().getAttribute("userMobile");
		
		if(mobNumber == null){
			mobNumber = "9052100567";
		}
		logger.info("Mobile Number from charts:"+mobNumber);
		//model = new object.. set values and set to MAV..
		
		/*SolrQuery query = new SolrQuery("id:*");
		query.set("facet", true);
		query.addFacetField("tweet_category");
		query.addSort("tweet_category",SolrQuery.ORDER.asc);
		query.setRows(0);*/
		
		DateFormat solrFormat = new SimpleDateFormat ("yyyy-MM-dd'T'hh:mm:ss'Z'");
		DateFormat dateFormat = new SimpleDateFormat ("E MMM d hh:mm:ss zzz yyyy");
		SimpleDateFormat newFormat = new SimpleDateFormat("MM-dd-yyyy");
		
		DateFormat dateFormatsolr = new SimpleDateFormat("yyyy-MM-dd");

		int pastdayscount = 7;
		
		SolrQuery query = null;
		QueryResponse response =null;
		
		ChartBean bean = new ChartBean();
		try{
			Collection<JSONObject> dataList = new ArrayList<JSONObject>();
			TreeMap<String,String> dataListObj = new TreeMap<String,String>();
			Collection<JSONObject> networkList = new ArrayList<JSONObject>();
			RandonGen rg = new RandonGen();
			//float i=1;
			while(pastdayscount>0){
				Calendar cal = Calendar.getInstance();
		        cal.add(Calendar.DATE, -pastdayscount);
		        Date todate1 = cal.getTime();    
		        String fromdate = dateFormatsolr.format(todate1);
		        query = new SolrQuery("cat:call AND mobile:" +mobNumber+" AND call_end:["+fromdate+"T00:00:00Z TO "+fromdate+"T23:59:59Z]");
		        query.setRows(0);
		        query.addFacetField("call_duartion");
		        response = homeservice.getServiceResponse(query);
		        
		        FacetField dataUsedFacetList = response.getFacetField("call_duartion");
				List<Count> dataValues = dataUsedFacetList.getValues();
				float dataTotal =0;
				for(Count dataEntry:dataValues){
					dataTotal = dataTotal + (Float.parseFloat(dataEntry.getName())) * dataEntry.getCount();
					//i++;
				}
				//dataTotal = dataTotal / rg.get2DigitNum();
				String userFormatDate = newFormat.format(todate1);
				
				JSONObject timeObject = new JSONObject();
				timeObject.put("date", userFormatDate);
				timeObject.put("duration", dataTotal);
				dataList.add(timeObject);
				dataListObj.put(userFormatDate, dataTotal+ " Mins") ;
				
		        
		        
		        
				pastdayscount = pastdayscount-1;
			}
			
			/*JSONObject timeObject = new JSONObject();
			timeObject.put("date", "11-03-2015");
			timeObject.put("duration", "03:34:42");
			dataList.add(timeObject);
			dataListObj.put("11-03-2015", "03:34:42");
			
			
			JSONObject timeObject1 = new JSONObject();
			timeObject1.put("date", "11-02-2015");
			timeObject1.put("duration", "05:34:22");
			dataList.add(timeObject1);
			dataListObj.put("11-02-2015", "05:34:22");
			
			JSONObject timeObject2 = new JSONObject();
			timeObject2.put("date", "11-01-2015");
			timeObject2.put("duration", "02:22:35");
			dataList.add(timeObject2);
			dataListObj.put("11-01-2015", "02:22:35");
			
			JSONObject timeObject3 = new JSONObject();
			timeObject3.put("date", "10-31-2015");
			timeObject3.put("duration", "10:50:09");
			dataList.add(timeObject3);
			dataListObj.put("10-31-2015", "10:50:09");
			
			JSONObject timeObject4 = new JSONObject();
			timeObject4.put("date", "10-30-2015");
			timeObject4.put("duration", "08:09:03");
			dataList.add(timeObject4);
			dataListObj.put("10-30-2015", "08:09:03");
			
			JSONObject timeObject5 = new JSONObject();
			timeObject5.put("date", "10-29-2015");
			timeObject5.put("duration", "06:34:36");
			dataList.add(timeObject5);
			dataListObj.put("10-29-2015", "06:34:36");
			
			JSONObject timeObject6 = new JSONObject();
			timeObject6.put("date", "10-28-2015");
			timeObject6.put("duration", "09:31:59");
			dataList.add(timeObject6);
			dataListObj.put("10-28-2015", "09:31:59");*/
			Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -7);
	        Date todate1 = cal.getTime();    
	        String fromdate = dateFormatsolr.format(todate1);
	        
	        Calendar cal1 = Calendar.getInstance();
	        cal1.add(Calendar.DATE, -1);
	        Date todate2 = cal.getTime();    
	        String fromdate1 = dateFormatsolr.format(todate2);
			query = new SolrQuery("cat:call AND mobile:" +mobNumber+" AND call_end:["+fromdate+"T00:00:00Z TO "+fromdate1+"T23:59:59Z]");
			
			query.setRows(0);
	        query.addFacetField("recipient_network");
	        response = homeservice.getServiceResponse(query);
	        
	        FacetField dataUsedFacetList = response.getFacetField("recipient_network");
			List<Count> dataValues = dataUsedFacetList.getValues();
			float dataTotal =0;
			for(Count dataEntry:dataValues){
				//dataTotal = dataTotal + (Float.parseFloat(dataEntry.getName())) * dataEntry.getCount();
				//i++;
				JSONObject timeObj = new JSONObject();
				timeObj.put("label", dataEntry.getName());
				timeObj.put("data", dataEntry.getCount());
				networkList.add(timeObj);
			}
			
			JSONObject finalObj = new JSONObject();
			finalObj.put("dataListByDate", dataList);
			bean.setDataListRecords(dataListObj);
			bean.setDataUsageList(finalObj);
			//network
			/*JSONObject timeObj = new JSONObject();
			timeObj.put("label", "Verizon");
			timeObj.put("data", rg.get2DigitNum());
			networkList.add(timeObj);
			
			JSONObject timeObj1 = new JSONObject();
			timeObj1.put("label", "TMobile");
			timeObj1.put("data", rg.get2DigitNum());
			networkList.add(timeObj1);

			JSONObject timeObj2 = new JSONObject();
			timeObj2.put("label", "AT&T");
			timeObj2.put("data", rg.get2DigitNum());
			networkList.add(timeObj2);
			
			JSONObject timeObj3 = new JSONObject();
			timeObj3.put("label", "centuryLink");
			timeObj3.put("data", rg.get2DigitNum());
			networkList.add(timeObj3);			
			
			JSONObject timeObj4 = new JSONObject();
			timeObj4.put("label", "Other");
			timeObj4.put("data", rg.get2DigitNum());
			networkList.add(timeObj4);*/
			
			JSONObject finalNetObj = new JSONObject();
			finalNetObj.put("networkListPast", networkList);
			bean.setNetworkDataList(finalNetObj);
			
			/*QueryResponse response = homeservice.getServiceResponse(query);
			Collection<JSONObject> productsList = new ArrayList<JSONObject>();
			Collection<JSONObject> tabletsAndSmartPhones = new ArrayList<JSONObject>();
			if(response!=null){
				FacetField categoryFacet = response.getFacetField("tweet_category");
				List<Count> list =categoryFacet.getValues();
				Iterator itr =list.iterator();
				Collection<String> productKeys = new ArrayList<String>();
				Collection<String> smartPhoneKeys = new ArrayList<String>();
				while(itr.hasNext()){
					Count categoryObj = (Count)itr.next();
					if(categoryObj!=null){
						String categoryName = categoryObj.getName();
						if((categoryName.contains("Products") || categoryName.contains("Product")) && categoryName.contains("_")){
							//System.out.println(categoryName.split("_")[2]);
							String subCatName = categoryName.split("_")[2];
							JSONObject tempJSON = new JSONObject();
							if(!productKeys.contains(subCatName)){
								productKeys.add(subCatName);
								tempJSON.put("label", subCatName);
								tempJSON.put("data", categoryObj.getCount());
								productsList.add(tempJSON);
							}
							
						}else if((categoryName.contains("Smartphones") || categoryName.contains("Tablets")) && categoryName.contains("_")){
							String subCatName = categoryName.split("_")[2];
							JSONObject tempJSON = new JSONObject();
							if(!smartPhoneKeys.contains(subCatName)){
								smartPhoneKeys.add(subCatName);
								tempJSON.put("label", subCatName);
								tempJSON.put("data", categoryObj.getCount());
								tabletsAndSmartPhones.add(tempJSON);
							}
						}
					}
				}
			}
			JSONObject productsJSON = new JSONObject();
			productsJSON.put("productList", productsList);
			JSONObject smartPhoneJSON = new JSONObject();
			smartPhoneJSON.put("phonesAndTablets", tabletsAndSmartPhones);
			bean.setProductsJSON(productsJSON);
			bean.setSmartphoneAndTablets(smartPhoneJSON);
			
			
			query.addFacetPivotField("tweetPostedTime,sentimentScore");
			query.addSort("tweetPostedTime",SolrQuery.ORDER.asc);
			query.setRows(0);
			DateFormat dateFormat1 = new SimpleDateFormat ("E MMM d hh:mm:ss zzz yyyy");
			SimpleDateFormat newFormat = new SimpleDateFormat("MM-dd-yyyy");
			Collection<JSONObject> sentimentObjList = new ArrayList<JSONObject>();
			JSONObject tempObj = new JSONObject();
			JSONObject finalObj = new JSONObject();
			response = homeservice.getServiceResponse(query);
			List<Date> myList = new ArrayList<Date>();
			if(response!=null){
				System.out.println(response);
				NamedList<List<PivotField>> pivotNamedList = response.getFacetPivot();
				System.out.println(pivotNamedList.size());
				for( int i=0;i<pivotNamedList.size();i++){
					List<PivotField> pvotList = pivotNamedList.getVal(i);
					for(PivotField pf:pvotList){
						if(pf!=null){
							System.out.println(pf.getValue());
							Date beforeParse = dateFormat1.parse(pf.getValue().toString());
							myList.add(beforeParse);
							JSONObject sentimentObj = new JSONObject();
							
							sentimentObj.put("period", newFormat.format(beforeParse));
							//System.out.println(pf.getField());
							List<PivotField> sentimentList = pf.getPivot();
							int negativeTotal =0;
							int neutralTotal =0;
							int positiveTotal =0;
							for(PivotField sentimentpivot:sentimentList){
								
								if(sentimentpivot!=null){
									if(sentimentpivot.getValue()!=null){
										int fullCatName = (Integer)sentimentpivot.getValue();
										if(fullCatName ==0){
											negativeTotal = negativeTotal + sentimentpivot.getCount();
										}else if(fullCatName == 1 || fullCatName ==2 ){
											neutralTotal = neutralTotal + sentimentpivot.getCount();
										}else if(fullCatName ==3 || fullCatName==4){
											positiveTotal = positiveTotal + sentimentpivot.getCount();
										}
									}
									
								}
							}
							sentimentObj.put("negative", negativeTotal);
							sentimentObj.put("positive", positiveTotal);
							sentimentObj.put("neutral", neutralTotal);
							tempObj.put(newFormat.format(beforeParse), sentimentObj);
						}
						
					}
					
				}
			}
			Collections.sort(myList);
			Iterator<Date> itr = myList.iterator();
			while(itr.hasNext()){
				
				Date inputDate = dateFormat1.parse(itr.next().toString());
				
				String tempKey = newFormat.format(inputDate);
				System.out.println(tempKey);
				JSONObject newObj = (JSONObject)tempObj.get(tempKey);
				sentimentObjList.add(newObj);
				
			}
			finalObj.put("sentimentData", sentimentObjList);
			bean.setSentimentDataObject(finalObj);*/
		}catch(Exception e){
			e.printStackTrace();
		}
		ModelAndView mav = new ModelAndView("charts", "model", bean);
		logger.info("bye now");
		return mav;
	}
	
	@RequestMapping(value = "/getCallByDate", method = RequestMethod.GET)
	@ResponseBody
	public String getDataByDate(@RequestParam("dateVal") String dateVal,HttpServletRequest request) {
		JSONObject finalRes = new JSONObject();
		Collection<JSONObject> recipentList = new ArrayList<JSONObject>();
		System.out.println("dateVal:"+dateVal);
		
		DateFormat solrFormat = new SimpleDateFormat ("yyyy-MM-dd'T'hh:mm:ss'Z'");
		DateFormat dateFormat = new SimpleDateFormat ("E MMM d hh:mm:ss zzz yyyy");
		SimpleDateFormat newFormat = new SimpleDateFormat("MM-dd-yyyy");
		
		DateFormat dateFormatsolr = new SimpleDateFormat("MM-dd-yyyy");
		DateFormat dateFormatsolr1 = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        Date todate1 = cal.getTime();    
        Date fromdate12 = null;
		try {
			fromdate12 = dateFormatsolr.parse(dateVal);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
        String fromdate = dateFormatsolr1.format(fromdate12);
        String mobNumber = "";//"9052100567";//read from login..
		mobNumber = (String)request.getSession().getAttribute("userMobile");
		
		if(mobNumber == null){
			mobNumber = "9052100567";
		}
		logger.info("Mobile Number from charts:"+mobNumber);
        
		SolrQuery query = new SolrQuery("cat:call AND mobile:" +mobNumber+" AND call_end:["+fromdate+"T00:00:00Z TO "+fromdate+"T23:59:59Z]");
		
		query.setRows(0);
        query.addFacetField("recipient_network");
        QueryResponse response = homeservice.getServiceResponse(query);
        
        FacetField dataUsedFacetList = response.getFacetField("recipient_network");
		List<Count> dataValues = dataUsedFacetList.getValues();
		float dataTotal =0;
		for(Count dataEntry:dataValues){
			//dataTotal = dataTotal + (Float.parseFloat(dataEntry.getName())) * dataEntry.getCount();
			//i++;
			JSONObject timeObj = new JSONObject();
			timeObj.put("label", dataEntry.getName());
			timeObj.put("data", dataEntry.getCount());
			recipentList.add(timeObj);
		}
		
		/*if("11-03-2015".equals(dateVal)){
			JSONObject timeObj = new JSONObject();
			timeObj.put("label", "Verizon");
			timeObj.put("data", 19.5);
			recipentList.add(timeObj);
			
			JSONObject timeObj1 = new JSONObject();
			timeObj1.put("label", "TMobile");
			timeObj1.put("data", 4.5);
			recipentList.add(timeObj1);
	
			JSONObject timeObj2 = new JSONObject();
			timeObj2.put("label", "AT&T");
			timeObj2.put("data", 36.6);
			recipentList.add(timeObj2);
			
			JSONObject timeObj3 = new JSONObject();
			timeObj3.put("label", "centuryLink");
			timeObj3.put("data", 36.3);
			recipentList.add(timeObj3);
			
			JSONObject timeObj4 = new JSONObject();
			timeObj4.put("label", "Other");
			timeObj4.put("data", 0.8);
			recipentList.add(timeObj4);
		}else if("11-02-2015".equals(dateVal)){
			JSONObject timeObj = new JSONObject();
			timeObj.put("label", "Verizon");
			timeObj.put("data", 10.5);
			recipentList.add(timeObj);
			
			JSONObject timeObj1 = new JSONObject();
			timeObj1.put("label", "TMobile");
			timeObj1.put("data", 9.5);
			recipentList.add(timeObj1);
	
			JSONObject timeObj2 = new JSONObject();
			timeObj2.put("label", "AT&T");
			timeObj2.put("data", 30.6);
			recipentList.add(timeObj2);
			
			JSONObject timeObj3 = new JSONObject();
			timeObj3.put("label", "centuryLink");
			timeObj3.put("data", 20.3);
			recipentList.add(timeObj3);
			
			JSONObject timeObj4 = new JSONObject();
			timeObj4.put("label", "Other");
			timeObj4.put("data", 5.8);
			recipentList.add(timeObj4);
		}else if("11-01-2015".equals(dateVal)){
			JSONObject timeObj = new JSONObject();
			timeObj.put("label", "Verizon");
			timeObj.put("data", 10.5);
			recipentList.add(timeObj);
			
			JSONObject timeObj1 = new JSONObject();
			timeObj1.put("label", "TMobile");
			timeObj1.put("data", 7.5);
			recipentList.add(timeObj1);
	
			JSONObject timeObj2 = new JSONObject();
			timeObj2.put("label", "AT&T");
			timeObj2.put("data", 9.6);
			recipentList.add(timeObj2);
			
			JSONObject timeObj3 = new JSONObject();
			timeObj3.put("label", "centuryLink");
			timeObj3.put("data", 8.3);
			recipentList.add(timeObj3);
			
			JSONObject timeObj4 = new JSONObject();
			timeObj4.put("label", "Other");
			timeObj4.put("data", 5.8);
			recipentList.add(timeObj4);
		}else if("10-31-2015".equals(dateVal)){
			JSONObject timeObj = new JSONObject();
			timeObj.put("label", "Verizon");
			timeObj.put("data", 12.5);
			recipentList.add(timeObj);
			
			JSONObject timeObj1 = new JSONObject();
			timeObj1.put("label", "TMobile");
			timeObj1.put("data", 7.5);
			recipentList.add(timeObj1);
	
			JSONObject timeObj2 = new JSONObject();
			timeObj2.put("label", "AT&T");
			timeObj2.put("data", 40.6);
			recipentList.add(timeObj2);
			
			JSONObject timeObj3 = new JSONObject();
			timeObj3.put("label", "centuryLink");
			timeObj3.put("data", 15.3);
			recipentList.add(timeObj3);
			
			JSONObject timeObj4 = new JSONObject();
			timeObj4.put("label", "Other");
			timeObj4.put("data", 4.8);
			recipentList.add(timeObj4);
		}else if("10-30-2015".equals(dateVal)){
			JSONObject timeObj = new JSONObject();
			timeObj.put("label", "Verizon");
			timeObj.put("data", 29.5);
			recipentList.add(timeObj);
			
			JSONObject timeObj1 = new JSONObject();
			timeObj1.put("label", "TMobile");
			timeObj1.put("data", 4.5);
			recipentList.add(timeObj1);
	
			JSONObject timeObj2 = new JSONObject();
			timeObj2.put("label", "AT&T");
			timeObj2.put("data", 10.6);
			recipentList.add(timeObj2);
			
			JSONObject timeObj3 = new JSONObject();
			timeObj3.put("label", "centuryLink");
			timeObj3.put("data", 20.3);
			recipentList.add(timeObj3);
			
			JSONObject timeObj4 = new JSONObject();
			timeObj4.put("label", "Other");
			timeObj4.put("data", 2.8);
			recipentList.add(timeObj4);
		}else if("10-29-2015".equals(dateVal)){
			JSONObject timeObj = new JSONObject();
			timeObj.put("label", "Verizon");
			timeObj.put("data", 10.5);
			recipentList.add(timeObj);
			
			JSONObject timeObj1 = new JSONObject();
			timeObj1.put("label", "TMobile");
			timeObj1.put("data", 20.5);
			recipentList.add(timeObj1);
	
			JSONObject timeObj2 = new JSONObject();
			timeObj2.put("label", "AT&T");
			timeObj2.put("data", 8.6);
			recipentList.add(timeObj2);
			
			JSONObject timeObj3 = new JSONObject();
			timeObj3.put("label", "centuryLink");
			timeObj3.put("data", 19.3);
			recipentList.add(timeObj3);
			
			JSONObject timeObj4 = new JSONObject();
			timeObj4.put("label", "Other");
			timeObj4.put("data", 7.8);
			recipentList.add(timeObj4);
		}else {
			JSONObject timeObj = new JSONObject();
			timeObj.put("label", "Verizon");
			timeObj.put("data", 18.5);
			recipentList.add(timeObj);
			
			JSONObject timeObj1 = new JSONObject();
			timeObj1.put("label", "TMobile");
			timeObj1.put("data", 14.5);
			recipentList.add(timeObj1);
	
			JSONObject timeObj2 = new JSONObject();
			timeObj2.put("label", "AT&T");
			timeObj2.put("data", 25.6);
			recipentList.add(timeObj2);
			
			JSONObject timeObj3 = new JSONObject();
			timeObj3.put("label", "centuryLink");
			timeObj3.put("data", 30.3);
			recipentList.add(timeObj3);
			
			JSONObject timeObj4 = new JSONObject();
			timeObj4.put("label", "Other");
			timeObj4.put("data", 6.8);
			recipentList.add(timeObj4);
		}*/
				
		finalRes.put("recipientListByDate", recipentList);
		
		return finalRes.toString();
	}
	
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public ModelAndView search(Locale locale, Model model) {
		
		logger.info("Welcome Search! The client locale is {}.", locale);
		
		
		ModelAndView mav = new ModelAndView("search", "model", "");
		logger.info("bye now");
		return mav;
	}
	@RequestMapping(value = "/searchtweetresponse", method = RequestMethod.GET)
	@ResponseBody
	
	public String getTweets(@RequestParam("search") String searchKey,@RequestParam("start") int start,@RequestParam("end") int end) {
		
		logger.info("Welcome to Tweets");
		JSONObject finalObject = new JSONObject();
		
		int startRow =0;
		int endRow=10;
		//String tweetType="*";
		
		if(start>startRow){
			startRow =start;
		}
		if(end>endRow){
			endRow=end;
		}
		SolrQuery query = new SolrQuery("tweet_content:*"+searchKey+"*");
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
			 finalObject.put("total", totalCount); 
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
		
		finalObject.put("tweetList", objectList);
		
		query.set("facet", true);
		query.addFacetField("tweetPostedTime");
		query.addSort("tweetPostedTime",SolrQuery.ORDER.asc);
		query.setRows(0);
		QueryResponse response = homeservice.getServiceResponse(query);
		//2015-07-30T12:10:31Z
		DateFormat dateFormat1 = new SimpleDateFormat ("yyyy-MM-dd");
		SimpleDateFormat newFormat = new SimpleDateFormat("MM-dd-yyyy");
		DateFormat dateFormat2 = new SimpleDateFormat ("E MMM d hh:mm:ss zzz yyyy");
		List<Date> myList = new ArrayList<Date>();
		//Collection<JSONObject> tempList = new ArrayList<JSONObject>();
		Collection<JSONObject> dateCountList = new ArrayList<JSONObject>();
		JSONObject tempObj = new JSONObject();
		try{
			if(response!=null){
				FacetField timeFacet = response.getFacetField("tweetPostedTime");
				List<Count> list =timeFacet.getValues();
				Iterator itr =list.iterator();
				//Collection<String> productKeys = new ArrayList<String>();
				//Collection<String> smartPhoneKeys = new ArrayList<String>();
				while(itr.hasNext()){
					Count timeObj = (Count)itr.next();
					if(timeObj!=null){
						String timeName = timeObj.getName();
						String originalTime = timeName.split("T")[0];
						Date beforeParse = dateFormat1.parse(originalTime);
						myList.add(beforeParse);
						JSONObject timeObject = new JSONObject();
						timeObject.put("date", newFormat.format(beforeParse));
						timeObject.put("count", timeObj.getCount());
						//tempList.add(timeObject);
						tempObj.put(newFormat.format(beforeParse), timeObject);
					}
				}
			}
			Collections.sort(myList);
			Iterator<Date> itr = myList.iterator();
			while(itr.hasNext()){
				
				Date inputDate = dateFormat2.parse(itr.next().toString());
				
				String tempKey = newFormat.format(inputDate);
				System.out.println(tempKey);
				JSONObject newObj = (JSONObject)tempObj.get(tempKey);
				dateCountList.add(newObj);
			}
			finalObject.put("dateList", dateCountList);
			
		}catch(Exception e){
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		logger.info("bye Tweets");
		return finalObject.toString();
	}
}
