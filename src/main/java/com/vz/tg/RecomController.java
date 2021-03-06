package com.vz.tg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.vz.tg.model.RecomBean;
import com.vz.tg.services.HomeService;

/**
 * Handles requests for the application home page.
 */
@Controller
public class RecomController {
	
	private static final Logger logger = LoggerFactory.getLogger(ChartController.class);
	@Autowired
	HomeService homeservice ;
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/recom", method = RequestMethod.GET)
	public ModelAndView home(Locale locale, Model model,HttpServletRequest request) {
		
		logger.info("Welcome Charts! The client locale is {}.", locale);
		
		//model = new object.. set values and set to MAV..
		
		SolrQuery query = new SolrQuery("id:*");
		query.set("facet", true);
		query.addFacetField("tweet_category");
		query.addSort("tweet_category",SolrQuery.ORDER.asc);
		query.setRows(0);
		RecomBean bean = new RecomBean();
		try{
			Collection<JSONObject> dataList = new ArrayList<JSONObject>();
			LinkedHashMap<String,String> dataListObj = new LinkedHashMap<String,String>();
			Collection<JSONObject> networkList = new ArrayList<JSONObject>();
			
			Collection<JSONObject> calldataList = new ArrayList<JSONObject>();
			LinkedHashMap<String,Float> calldataListObj = new LinkedHashMap<String,Float>();
			
			
			
			JSONObject timeObject5 = new JSONObject();
			timeObject5.put("date", "May");
			timeObject5.put("duration", "650");
			dataList.add(timeObject5);
			dataListObj.put("May", "650 MB");
			
			JSONObject timeObject4 = new JSONObject();
			timeObject4.put("date", "June");
			timeObject4.put("duration", "850");
			dataList.add(timeObject4);
			dataListObj.put("June", "850 MB");
			
			JSONObject timeObject3 = new JSONObject();
			timeObject3.put("date", "July");
			timeObject3.put("duration", "750");
			dataList.add(timeObject3);
			dataListObj.put("July", "750 MB");
			
			JSONObject timeObject2 = new JSONObject();
			timeObject2.put("date", "August");
			timeObject2.put("duration", "550");
			dataList.add(timeObject2);
			dataListObj.put("August", "550 MB");
			
			JSONObject timeObject1 = new JSONObject();
			timeObject1.put("date", "September");
			timeObject1.put("duration", "700");
			dataList.add(timeObject1);
			dataListObj.put("September", "700 MB");
			
			JSONObject timeObject = new JSONObject();
			timeObject.put("date", "October");
			timeObject.put("duration", "650");
			dataList.add(timeObject);
			dataListObj.put("October", "650 MB");
					
			JSONObject finalObj = new JSONObject();
			finalObj.put("dataListByDate", dataList);
			bean.setDataListRecords(dataListObj);
			bean.setDataUsageList(finalObj);
			
			// calculate mean and plan
			int mean = 0;
			int i = 0;
			for(JSONObject jsonObj : dataList){
				mean = mean + Integer.parseInt(jsonObj.get("duration").toString());
				i++;
			}
			
			int meanavg = mean / i;
			
			//plans
			Collection<JSONObject> planList = new ArrayList<JSONObject>();
			JSONObject plan1 = new JSONObject();
			plan1.put("plan", 700);
			planList.add(plan1);
			
			JSONObject plan2 = new JSONObject();
			plan2.put("plan", 800);
			planList.add(plan2);
			
			JSONObject plan3 = new JSONObject();
			plan3.put("plan", 900);
			planList.add(plan1);
			
			for(JSONObject planObj : planList){
				int plan = Integer.parseInt(planObj.get("plan").toString());
				if(meanavg <= plan){
					bean.setSelPlan(plan);
				}
				break;
			}
			
			String mobNumber = (String)request.getSession().getAttribute("userMobile");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			int past6Months = 6;
			while(past6Months>0){
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH, -past6Months);
				calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date pasttMonthFirstDay = calendar.getTime();
				calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date pastMonthLastDay = calendar.getTime();
				
				String fromDate = sdf.format(pasttMonthFirstDay);
				String toDate = sdf.format(pastMonthLastDay);
				
				query = new SolrQuery("cat:call AND mobile:" +mobNumber+" AND call_end:["+fromDate+"T00:00:00Z TO "+toDate+"T23:59:59Z]");
				query.setRows(0);
				query.setFacet(true);
				query.addFacetField("call_duartion");
				
				QueryResponse response = homeservice.getServiceResponse(query);
				
				if(response!=null){
					FacetField dataUsedFacetList = response.getFacetField("call_duartion");
					List<Count> dataValues = dataUsedFacetList.getValues();
					float dataTotal =0;
					for(Count dataEntry:dataValues){
						if(dataEntry.getCount()>0){
							dataTotal = dataTotal + (Float.parseFloat(dataEntry.getName())) * dataEntry.getCount();
						}else{
							break;
						}
						//i++;
					}
					//dataTotal = dataTotal / rg.get2DigitNum();
					String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
					
					JSONObject calltimeObject = new JSONObject();
					calltimeObject.put("date", month);
					calltimeObject.put("duration", dataTotal);
					calldataList.add(calltimeObject);
					calldataListObj.put(month, dataTotal);
				}
				past6Months = past6Months-1;
			}
			
			
			
			//call usage
			/*JSONObject calltimeObject = new JSONObject();
			calltimeObject.put("date", "October");
			calltimeObject.put("duration", "90");
			calldataList.add(calltimeObject);
			calldataListObj.put("10-October", "90 hrs");
			
			
			JSONObject calltimeObject1 = new JSONObject();
			calltimeObject1.put("date", "September");
			calltimeObject1.put("duration", "150");
			calldataList.add(calltimeObject1);
			calldataListObj.put("09-September", "150 hrs");
			
			JSONObject calltimeObject2 = new JSONObject();
			calltimeObject2.put("date", "August");
			calltimeObject2.put("duration", "60");
			calldataList.add(calltimeObject2);
			calldataListObj.put("08-August", "60 hrs");
			
			JSONObject calltimeObject3 = new JSONObject();
			calltimeObject3.put("date", "July");
			calltimeObject3.put("duration", "300");
			calldataList.add(calltimeObject3);
			calldataListObj.put("07-July", "300 hrs");
			
			JSONObject calltimeObject4 = new JSONObject();
			calltimeObject4.put("date", "June");
			calltimeObject4.put("duration", "240");
			calldataList.add(calltimeObject4);
			calldataListObj.put("06-June", "240 hrs");
			
			JSONObject calltimeObject5 = new JSONObject();
			calltimeObject5.put("date", "May");
			calltimeObject5.put("duration", "180");
			calldataList.add(calltimeObject5);
			calldataListObj.put("05-May", "180 hrs");*/
			
			JSONObject callfinalObj = new JSONObject();
			callfinalObj.put("calldataListByDate", calldataList);
			bean.setCalldataListRecords(calldataListObj);
			bean.setCalldataUsageList(callfinalObj);
			
			//network
			/*JSONObject timeObj = new JSONObject();
			timeObj.put("label", "Verizon");
			timeObj.put("data", 25.5);
			networkList.add(timeObj);
			
			JSONObject timeObj1 = new JSONObject();
			timeObj1.put("label", "TMobile");
			timeObj1.put("data", 14.5);
			networkList.add(timeObj1);

			JSONObject timeObj2 = new JSONObject();
			timeObj2.put("label", "AT&T");
			timeObj2.put("data", 30.6);
			networkList.add(timeObj2);
			
			JSONObject timeObj3 = new JSONObject();
			timeObj3.put("label", "centuryLink");
			timeObj3.put("data", 32.3);
			networkList.add(timeObj3);			
			
			JSONObject timeObj4 = new JSONObject();
			timeObj4.put("label", "Other");
			timeObj4.put("data", 10.8);
			networkList.add(timeObj4);
			
			JSONObject finalNetObj = new JSONObject();
			finalNetObj.put("networkListPast", networkList);
			bean.setNetworkDataList(finalNetObj);*/
			
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
		ModelAndView mav = new ModelAndView("recom", "model", bean);
		logger.info("bye now");
		return mav;
	}
	
}
