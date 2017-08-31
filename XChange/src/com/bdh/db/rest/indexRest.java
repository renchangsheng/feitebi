package com.bdh.db.rest;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;

import com.bdh.db.dao.BalanceDao;
import com.bdh.db.dao.indexDao;
import com.bdh.db.entry.Account;
import com.bdh.db.entry.AccountBalance;
import com.bdh.db.entry.Bookmark;
import com.bdh.db.entry.Exchang;
import com.bdh.db.entry.PagableData;
import com.bdh.db.entry.StatusBean;
import com.bdh.db.entry.Strategy;
import com.bdh.db.entry.indexMyfollowing;
import com.bdh.db.view.Fansnumber;
import com.sun.jersey.api.json.JSONWithPadding;

@Path("/index")
@Produces({ "application/x-javascript", "application/json", "application/xml" })
public class indexRest {
	/***
	 * api
	 * **/
	    @GET
		@Produces("application/json")
		@Path("/platform_api")
	    public JSONWithPadding queryApis(@QueryParam("userId") String userId) throws Exception{
	    	return new JSONWithPadding(new GenericEntity<PagableData<Exchang>>(new BalanceDao().queryApis(userId)){
	    	},"platform_api");
	   }
	    
	
	    /**
	     * balance数据
	     * **/
	    @GET
		@Produces("application/json")
		@Path("/platform_getPlatformSummary")
	    public JSONWithPadding getPlatformSummary(@QueryParam("userId") String userId,
	    										  @QueryParam("platformindex") String platformindex,
	    										  @QueryParam("apikey") String apikey,
	    										  @QueryParam("apisecret") String apisecret) throws Exception{
	    	return new JSONWithPadding(new GenericEntity<Map<String, Collection<AccountBalance>>>(new BalanceDao().getAllPlatformSummary(userId, platformindex, apikey, apisecret)){
	    	},"getPlatformSummary");
	   }
	    
	
	
	
	/**
	 * 显示我关注的币
	 * **/
	 	@POST
		@Produces("application/json")
		@Path("/MyBookmark")
	    public JSONWithPadding MyBookmark(@FormParam("userid") String userid){
	    	return new JSONWithPadding(new GenericEntity<PagableData<Bookmark>>(new BalanceDao().MyBookmark(userid)){
	    	},"MyBookmark");
	    }
	 	//显示信息
	 			@GET
	 			@Produces("application/json")
	 			@Path("/getAccountSummary")
	 		    public JSONWithPadding getAccountSummary(@QueryParam("userid") String userid) throws Exception{
	 		    	return new JSONWithPadding(new GenericEntity<PagableData<Account>>(new BalanceDao().getAccountSummary(userid)){
	 		    	},"getAccountSummary");
	 		    }
	
	//显示智能合约
	 	
	    @POST
		@Produces("application/json")
		@Path("/smartdeal")
	    public JSONWithPadding smartdeal(@FormParam("userid") String userid){
	    	return new JSONWithPadding(new GenericEntity<PagableData<Strategy>>(new indexDao().smartDeal(userid)){
	    	},"smartdeal");
	    }
	    /**
		 *查询买卖
		 * */	
		    @POST
			@Produces("application/json")
			@Path("/delsmart")
		    public JSONWithPadding delsmart(@FormParam("id") String id){
		    	return new JSONWithPadding(new GenericEntity<StatusBean>(new indexDao().delsmart(id)){
		    	},"delsmart");
		    }
		    //获取关注大师
		 /*   @GET
			@Produces("application/json")
			@Path("/getMymaster")
		    public JSONWithPadding getMymaster(@QueryParam("userid") String userid) throws Exception{
				return new JSONWithPadding(new GenericEntity<List<indexMyfollowing>>(new BalanceDao().getMymaster(userid)){
				},"getMymaster");
		        }*/
		    @GET
			@Produces("application/json")
			@Path("/getMymaster")
		    public JSONWithPadding fansnumber(@QueryParam("userid") String userid) throws Exception{
				return new JSONWithPadding(new GenericEntity<PagableData<Fansnumber>>(new BalanceDao().fansnumber(userid)){
				},"getMymaster");
		        }
		    
		    /**
		     * 获取我的订单信息
		     * */
		    
		  /*  @GET
			@Produces("application/json")
			@Path("/getMyAllOpenOrder")
		    public JSONWithPadding getMyAllOpenOrderList(@QueryParam("userid") String userid) throws Exception,IOException{
		    	System.out.println(userid);
		    	return new JSONWithPadding(new GenericEntity<Map<String, Collection<LimitOrder>>>(new testDao().getMyAllOpenOrderList(userid)){
		    	},"getMyAllOpenOrder");
	       }*/
		
}
