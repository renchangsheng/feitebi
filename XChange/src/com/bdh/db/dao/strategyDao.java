package com.bdh.db.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bittrex.v1.dto.marketdata.BittrexTicker;
import org.knowm.xchange.bittrex.v1.service.BittrexMarketDataServiceRaw;
import org.knowm.xchange.bleutrade.dto.marketdata.BleutradeTicker;
import org.knowm.xchange.bleutrade.service.BleutradeMarketDataServiceRaw;
import org.knowm.xchange.btce.v3.service.BTCEMarketDataServiceRaw;
import org.knowm.xchange.btctrade.service.BTCTradeMarketDataService;
import org.knowm.xchange.bter.dto.marketdata.BTERTicker;
import org.knowm.xchange.bter.service.BTERMarketDataServiceRaw;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.hitbtc.dto.marketdata.HitbtcTicker;
import org.knowm.xchange.hitbtc.service.HitbtcMarketDataService;
import org.knowm.xchange.huobi.service.HuobiMarketDataService;
import org.knowm.xchange.kraken.service.KrakenMarketDataServiceRaw;
import org.knowm.xchange.poloniex.dto.marketdata.PoloniexMarketData;
import org.knowm.xchange.poloniex.service.PoloniexMarketDataServiceRaw;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.yobit.service.YoBitMarketDataService;

import com.alibaba.fastjson.JSONException;
import com.bdh.db.entry.AccountBalance;
import com.bdh.db.entry.Apis;
import com.bdh.db.entry.Exchang;
import com.bdh.db.entry.Following;
import com.bdh.db.entry.HistoryLimitOrder;
import com.bdh.db.entry.HistoryOrder;
import com.bdh.db.entry.PagableData;
import com.bdh.db.entry.StatusBean;
import com.bdh.db.entry.Strategy;
import com.bdh.db.entry.smartOrder;
import com.bdh.db.util.DBUtil;
import com.bdh.db.util.FunctionSet;

import org.junit.Test;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.poloniex.PoloniexExchange;
import org.knowm.xchange.service.marketdata.MarketDataService;

import com.bdh.db.entry.Account;

public class strategyDao {

	//查询智能买卖
	public PagableData<smartOrder> getsmartorder(String userid){
		Connection conn= null;
		PreparedStatement stmt= null;
		PagableData<smartOrder> pd=new PagableData<smartOrder>();
		try {
			conn = DBUtil.getInstance().getConnection();
			StringBuffer sql=new StringBuffer();
			sql.append("SELECT * from strategy where strategyFlag=0 and userid='"+FunctionSet.filt(userid)+"'");
			stmt=conn.prepareStatement(sql.toString());
			List<smartOrder> olist=DBUtil.getInstance().convert(stmt.executeQuery(), smartOrder.class);
			
			if(!olist.isEmpty()){
				pd.setDataList(olist);
			}
			sql=null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DBUtil.getInstance().close(stmt);
			DBUtil.getInstance().close(conn);
		}
		return pd;
	}	
	
	/**首页智能策略**/
	public PagableData<smartOrder> getsmartorderTriker(String userid){
		Connection conn= null;
		PreparedStatement stmt= null;
		PagableData<smartOrder> pd=new PagableData<smartOrder>();
		try {
			conn = DBUtil.getInstance().getConnection();
			StringBuffer sql=new StringBuffer();
			sql.append("SELECT * from strategy where strategyFlag=0 and userid='"+FunctionSet.filt(userid)+"'");
			stmt=conn.prepareStatement(sql.toString());
			List<smartOrder> olist=DBUtil.getInstance().convert(stmt.executeQuery(), smartOrder.class);
			if(!olist.isEmpty()){
				Map<String, PoloniexMarketData> tickermap = null;
				for (int i = 0; i < olist.size(); i++) {
					tickermap = getAllTickerList(userid,olist.get(i).getPlatform().substring(0, 1).toUpperCase() + olist.get(i).getPlatform().substring(1));
					
					for (String key : tickermap.keySet()) {
						BigDecimal last = BigDecimal.ZERO;
						if (key.equals("BTC_"+olist.get(i).getCoinName().toUpperCase())) {
					
								last = tickermap.get(key).getLast();
							
							olist.get(i).setTickerlast(last.toString());
						}else{
							last = tickermap.get(key).getLast();
						}
					}
				}
				pd.setDataList(olist);
			}
			sql=null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DBUtil.getInstance().close(stmt);
			DBUtil.getInstance().close(conn);
		}
		return pd;
	}	
	
	
	//取消智能买卖
	public StatusBean cancelSmartorder(String id){
		StatusBean bean=new StatusBean();
		bean.setFlag(0);
		Connection conn= null;
		Statement stmt = null;
		try {
			conn= DBUtil.getInstance().getConnection();
			stmt = conn.createStatement();
			StringBuffer sql= new StringBuffer();
			sql.append("UPDATE strategy SET strategyFlag=2 where id='"+FunctionSet.filt(id)+"'");
			if(stmt.executeUpdate(sql.toString()) > 0){
		    	   bean.setFlag(1);
		        }
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			DBUtil.getInstance().close(stmt);
			DBUtil.getInstance().close(conn);
		}
		return bean;
	}
	

		
	//��������
	
	
	
	
	
	
	/**
	 * 查询api
	 * */
	
	public List<Exchang> loadAllApis(String userid) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DBUtil.getInstance().getConnection();
			stmt = conn.createStatement();
			String sql = "SELECT exchang.logo,apis.apiKey,apis.apiSecret FROM user,apis,exchang where user.userId=apis.userId "
					+ "and apis.isEnable=1 and apis.flag=1 AND apis.exchangid=exchang.exchangid and user.userId='"
					+ FunctionSet.filt(userid) + "'";
			List<Exchang> ds = DBUtil.getInstance().convert(
					stmt.executeQuery(sql), Exchang.class);
			return ds;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.getInstance().close(stmt);
			DBUtil.getInstance().close(conn);
		}
		return new ArrayList<Exchang>(0);
	}

	public String class4Name(String name) {
		// bitflyer cryptopia localbitcoins litebit bitlish exmo usecryptos
		// bitport.net cryptomate
		if (name.equalsIgnoreCase("c-cex")) {
			return "org.knowm.xchange." + name.substring(0, 1)
					+ name.substring(2) + "."
					+ name.substring(0, 1).toUpperCase()
					+ name.substring(2).toUpperCase() + "Exchange";
		}
		if (name.equalsIgnoreCase("cex.io")) {
			return "org.knowm.xchange." + name.substring(0, 1)
					+ name.substring(1, 3) + name.substring(4, 6) + "."
					+ name.substring(0, 1).toUpperCase() + name.substring(1, 3)
					+ name.substring(4, 6).toUpperCase() + "Exchange";
		}
		if (name.equalsIgnoreCase("bter")) {
			return "org.knowm.xchange." + name + "." + name.toUpperCase()
					+ "Exchange";
		}
		if (name.equalsIgnoreCase("bitfinex")) {
			return "org.knowm.xchange." + name.toLowerCase() + ".v1." + name
					+ "Exchange";
		}
		if (name.equalsIgnoreCase("bittrex")) {
			return "org.knowm.xchange." + name.toLowerCase() + ".v1."
					+ name.substring(0, 1).toUpperCase() + name.substring(1)
					+ "Exchange";
		}
		if (name.equalsIgnoreCase("livecoin")) {
			return "org.knowm.xchange." + name.toLowerCase() + "."
					+ name.substring(0, 1).toUpperCase() + name.substring(1)
					+ "Exchange";
		}
		if (name.equalsIgnoreCase("empoex")) {
			return "org.knowm.xchange." + name.toLowerCase() + "."
					+ name.substring(0, 1).toUpperCase() + name.substring(1, 4)
					+ name.substring(4, 6).toUpperCase() + "Exchange";
		}
		if (name.equalsIgnoreCase("the rock trading")) {
			return "org.knowm.xchange." + name.substring(0, 3)
					+ name.substring(4, 8) + "."
					+ name.substring(0, 1).toUpperCase() + name.substring(1, 3)
					+ name.substring(4, 5).toUpperCase() + name.substring(5, 8)
					+ "Exchange";
		}
		return "org.knowm.xchange." + name.toLowerCase() + "."
		+ name.substring(0, 1).toUpperCase() + name.substring(1)
		+ "Exchange";
	}
	
	/***
	 * 查询订单
	 * */
public PagableData<HistoryLimitOrder> getHistoryLimitOrder(String userId) throws Exception ,IOException{
		PagableData<HistoryLimitOrder> pd=new PagableData<HistoryLimitOrder>();
		List<Exchang> list=loadAllApis(userId);
		List<HistoryLimitOrder> historyLimitOrder= new ArrayList<HistoryLimitOrder>();
		Map<String, List<LimitOrder>> allPlatformOrderMap = new LinkedHashMap<String, List<LimitOrder>>();
		String[] a = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ExchangeSpecification spec = new ExchangeSpecification(
					class4Name(list.get(i).getLogo()));
			spec.setUserName(userId);
			spec.setApiKey(list.get(i).getApiKey());
			spec.setSecretKey(list.get(i).getApiSecret());
			a[i] = list.get(i).getLogo();
			Exchange bitstamp = ExchangeFactory.INSTANCE
					.createExchange(spec);
			OpenOrders uts = bitstamp.getTradeService().getOpenOrders();
			allPlatformOrderMap.put(list.get(i).getLogo(), uts.getOpenOrders());
			List<com.bdh.db.entry.LimitOrder> limitOrder= new ArrayList<com.bdh.db.entry.LimitOrder>();
			for (LimitOrder lo : uts.getOpenOrders()) {
				List<HistoryOrder> historyOrder= new ArrayList<HistoryOrder>();
				HistoryOrder h= new HistoryOrder();
				h.setAveragePrice(String.valueOf(lo.getAveragePrice()));
				h.setType(String.valueOf(lo.getType()));
				h.setCurrencyPair(String.valueOf(lo.getCurrencyPair()));
				h.setId(String.valueOf(lo.getId()));
				h.setStatus(String.valueOf(lo.getStatus()));
				h.setTimestamp(String.valueOf(lo.getTimestamp()));
				h.setTradableAmount(String.valueOf(lo.getTradableAmount()));
				historyOrder.add(h);
				com.bdh.db.entry.LimitOrder limitorder= new com.bdh.db.entry.LimitOrder();
				limitorder.setLimitPrice(String.valueOf(lo.getLimitPrice()));
				limitorder.setOrder(historyOrder);
				limitOrder.add(limitorder);
			}
			HistoryLimitOrder hlOrder= new HistoryLimitOrder();
			hlOrder.setPlatform(a[i]);
			hlOrder.setLimitOrder(limitOrder);
			historyLimitOrder.add(hlOrder);
		}
		pd.setDataList(historyLimitOrder);
		return pd;
	}

	
//ticker
	public Map getAllTickerList(String userId, String exchangeName)
			throws Exception {

		Exchange exchange = getExchangeByName(userId, exchangeName);
		if (exchange == null) {
			return new HashMap();
		}
		if (exchangeName.equalsIgnoreCase("Livecoin")) {
			/*
			 * JSONArray jsonArr = new
			 * JSONArray(readJsonFromUrl("https://api.livecoin.net/exchange/ticker"
			 * ));
			 * 
			 * Map<String, PoloniexMarketData> commMap = new HashMap<String,
			 * PoloniexMarketData>(); for (int i = 0; i < jsonArr.length(); i++)
			 * { JSONObject t = jsonArr.getJSONObject(i); PoloniexMarketData pmd
			 * = new PoloniexMarketData();
			 * 
			 * pmd.setBaseVolume(new BigDecimal(t.getDouble("volume")));
			 * pmd.setLast(new BigDecimal(t.getDouble("last")));
			 * pmd.setLowestAsk(new BigDecimal(t.getDouble("min_ask")));
			 * pmd.setHigh24hr(new BigDecimal(t.getDouble("high")));
			 * pmd.setLow24hr(new BigDecimal(t.getDouble("low")));
			 * pmd.setQuoteVolume(new BigDecimal(t.getDouble("volume")));
			 * pmd.setHighestBid(new BigDecimal(t.getDouble("max_bid"))); if
			 * (pmd.getLast().doubleValue() > 0) { BigDecimal pp =
			 * (pmd.getLast().setScale(8, RoundingMode.CEILING)
			 * .subtract(pmd.getLow24hr().setScale(8, RoundingMode.CEILING)))
			 * .divide(pmd.getLow24hr().setScale(8, RoundingMode.CEILING),
			 * RoundingMode.CEILING); pmd.setPercentChange(pp); }
			 * commMap.put(t.getString("symbol"), pmd); }
			 * 
			 * return commMap;
			 */

		} else if (exchangeName.equalsIgnoreCase("Bitfinex")) {
			/*
			 * JSONArray jsonArr = new JSONArray(readJsonFromUrl(
			 * "https://api.bitfinex.com/v2/tickers?symbols=tBTCUSD,tLTCUSD,tLTCBTC,tETHUSD,tETHBTC,tETCBTC,tETCUSD,tRRTUSD,tRRTBTC,tZECUSD,tZECBTC,tXMRUSD,tXMRBTC,tDSHUSD,tDSHBTC,tBCCBTC,tBCUBTC,tBCCUSD,tBCUUSD,tXRPUSD,tXRPBTC,tIOTUSD,tIOTBTC"
			 * ));
			 * 
			 * Map<String, PoloniexMarketData> commMap = new HashMap<String,
			 * PoloniexMarketData>(); for (int i = 0; i < jsonArr.length(); i++)
			 * { JSONArray value = jsonArr.getJSONArray(i); PoloniexMarketData
			 * pmd = new PoloniexMarketData(); String k = value.getString(0);
			 * 
			 * BigDecimal high = new BigDecimal(value.get(9).toString());
			 * BigDecimal low = new BigDecimal(value.get(10).toString());
			 * BigDecimal buy = new BigDecimal(value.get(1).toString());
			 * BigDecimal sell = new BigDecimal(value.get(3).toString());
			 * BigDecimal last = new BigDecimal(value.get(7).toString());
			 * BigDecimal vol = new BigDecimal(value.get(8).toString());
			 * BigDecimal pp = new BigDecimal(value.get(6).toString());
			 * 
			 * pmd.setBaseVolume(vol); pmd.setLast(last); pmd.setLowestAsk(buy);
			 * pmd.setHigh24hr(high); pmd.setLow24hr(low);
			 * pmd.setQuoteVolume(vol); pmd.setHighestBid(sell);
			 * 
			 * pmd.setPercentChange(pp); commMap.put(k.substring(1), pmd); }
			 * 
			 * return commMap;
			 */

		} else if (exchangeName.equalsIgnoreCase("Btc38")) {
			
			 /* Btc38AllTicker btc38 = new Btc38AllTicker(exchange);
			  System.out.println(1111111111);
			  Map<String, PoloniexMarketData> commMap = new HashMap<String,
			  PoloniexMarketData>(); Map<String, Btc38TickerReturn> tikers =
			  btc38.getBtc38AllTickers("CNY"); for (String k : tikers.keySet())
			  { Btc38Ticker t = tikers.get(k).getTicker(); PoloniexMarketData
			  pmd = new PoloniexMarketData();
			  
			  pmd.setBaseVolume(t.getVol()); pmd.setLast(t.getLast());
			  pmd.setLowestAsk(t.getSell()); pmd.setHigh24hr(t.getHigh());
			  pmd.setLow24hr(t.getLow()); pmd.setQuoteVolume(t.getVol());
			  pmd.setHighestBid(t.getHigh()); if (t.getLast() != null &&
			  t.getLast().doubleValue() > 0) { BigDecimal pp =
			  (t.getLast().setScale(8, RoundingMode.CEILING)
			  .subtract(t.getLow().setScale(8, RoundingMode.CEILING)))
			  .divide(t.getLow().setScale(8, RoundingMode.CEILING),
			  RoundingMode.CEILING); pmd.setPercentChange(pp); }
			  commMap.put(k.toString(), pmd); }
			  
			  return commMap;
			 */

		} else if (exchangeName.equalsIgnoreCase("BTER")) {
			BTERMarketDataServiceRaw pmds = new BTERMarketDataServiceRaw(
					exchange);
			Map<String, PoloniexMarketData> commMap = new HashMap<String, PoloniexMarketData>();
			Map<CurrencyPair, BTERTicker> tikers = pmds.getBTERTickers();
			for (CurrencyPair k : tikers.keySet()) {
				BTERTicker t = tikers.get(k);
				PoloniexMarketData pmd = new PoloniexMarketData();

				BigDecimal vol = t.getVolume("CNY");
				if (vol == null) {
					vol = t.getVolume("BTC");
				}
				if (vol == null) {
					vol = t.getVolume(k.toString().split("/")[0]);
				}
				pmd.setBaseVolume(vol);
				pmd.setLast(t.getLast());
				pmd.setLowestAsk(t.getSell());
				pmd.setHigh24hr(t.getHigh());
				pmd.setLow24hr(t.getLow());
				pmd.setQuoteVolume(vol);
				pmd.setHighestBid(t.getHigh());
				if (t.getLast().doubleValue() > 0) {
					BigDecimal pp = (t.getLast().setScale(8,
							RoundingMode.CEILING).subtract(t.getAvg().setScale(
							8, RoundingMode.CEILING))).divide(t.getAvg()
							.setScale(8, RoundingMode.CEILING),
							RoundingMode.CEILING);
					pmd.setPercentChange(pp);
				}
				commMap.put(k.toString(), pmd);
			}

			return commMap;

		} else if (exchangeName.equals("Poloniex")) {
			PoloniexMarketDataServiceRaw pmds = new PoloniexMarketDataServiceRaw(
					exchange);
			return pmds.getAllPoloniexTickers();
		} else if (exchangeName.equalsIgnoreCase("Bittrex")) {
			BittrexMarketDataServiceRaw bwdsr = new BittrexMarketDataServiceRaw(
					exchange);
			List<BittrexTicker> tikers = bwdsr.getBittrexTickers();
			Map<String, PoloniexMarketData> commMap = new HashMap<String, PoloniexMarketData>();
			for (BittrexTicker t : tikers) {
				PoloniexMarketData pmd = new PoloniexMarketData();
				pmd.setBaseVolume(t.getBaseVolume());
				pmd.setLast(t.getLast());
				pmd.setLowestAsk(t.getAsk());
				pmd.setHigh24hr(t.getHigh());
				pmd.setLow24hr(t.getLow());
				pmd.setQuoteVolume(t.getVolume());
				pmd.setHighestBid(t.getBid());
				if (t.getPrevDay().doubleValue() > 0) {
					BigDecimal pp = (t.getLast().setScale(8,
							RoundingMode.CEILING).subtract(t.getPrevDay()
							.setScale(8, RoundingMode.CEILING))).divide(t
							.getPrevDay().setScale(8, RoundingMode.CEILING),
							RoundingMode.CEILING);
					pmd.setPercentChange(pp);
				}
				commMap.put(t.getMarketName(), pmd);
			}

			return commMap;
		} else if (exchangeName.equalsIgnoreCase("Bleutrade")) {
			BleutradeMarketDataServiceRaw bwdsr = new BleutradeMarketDataServiceRaw(
					exchange);
			List<BleutradeTicker> tikers = bwdsr.getBleutradeTickers();
			Map<String, PoloniexMarketData> commMap = new HashMap<String, PoloniexMarketData>();
			for (BleutradeTicker t : tikers) {
				PoloniexMarketData pmd = new PoloniexMarketData();
				pmd.setBaseVolume(t.getBaseVolume());
				pmd.setLast(t.getLast());
				pmd.setLowestAsk(t.getAsk());
				pmd.setHigh24hr(t.getHigh());
				pmd.setLow24hr(t.getLow());
				pmd.setQuoteVolume(t.getVolume());
				pmd.setHighestBid(t.getBid());
				if (t.getPrevDay().doubleValue() > 0) {
					BigDecimal pp = (t.getLast().setScale(8,
							RoundingMode.CEILING).subtract(t.getPrevDay()
							.setScale(8, RoundingMode.CEILING))).divide(t
							.getPrevDay().setScale(8, RoundingMode.CEILING),
							RoundingMode.CEILING);
					pmd.setPercentChange(pp);
				}
				commMap.put(t.getMarketName(), pmd);
			}
			return commMap;
		} else if (exchangeName.equalsIgnoreCase("BTCE")) {
			BTCEMarketDataServiceRaw bwdsr = new BTCEMarketDataServiceRaw(
					exchange);
			// return bwdsr.getBTCETicker(currencyPair);
		} else if (exchangeName.equalsIgnoreCase("BTCTrade")) {
			BTCTradeMarketDataService bwdsr = (BTCTradeMarketDataService) exchange
					.getMarketDataService();
			// return bwdsr.getBTCTradeTicker(currencyPair);
		} else if (exchangeName.equalsIgnoreCase("Hitbtc")) {
			HitbtcMarketDataService bwdsr = (HitbtcMarketDataService) exchange
					.getMarketDataService();
			Map<String, HitbtcTicker> tikers = bwdsr.getHitbtcTickers();
			Map<String, PoloniexMarketData> commMap = new HashMap<String, PoloniexMarketData>();
			for (String k : tikers.keySet()) {
				HitbtcTicker t = tikers.get(k);
				PoloniexMarketData pmd = new PoloniexMarketData();
				pmd.setBaseVolume(t.getVolume());
				pmd.setLast(t.getLast());
				pmd.setLowestAsk(t.getAsk());
				pmd.setHigh24hr(t.getHigh());
				pmd.setLow24hr(t.getLow());
				pmd.setQuoteVolume(t.getVolumeQuote());
				pmd.setHighestBid(t.getBid());
				if (t.getOpen().doubleValue() > 0) {
					BigDecimal pp = (t.getLast().setScale(8,
							RoundingMode.CEILING).subtract(t.getOpen()
							.setScale(8, RoundingMode.CEILING))).divide(t
							.getOpen().setScale(8, RoundingMode.CEILING),
							RoundingMode.CEILING);
					pmd.setPercentChange(pp);
				}
				commMap.put(k, pmd);
			}
			return commMap;
		} else if (exchangeName.equalsIgnoreCase("Huobi")) {
			HuobiMarketDataService bwdsr = (HuobiMarketDataService) exchange
					.getMarketDataService();
			// return bwdsr.getTicker(currencyPair);
		} else if (exchangeName.equalsIgnoreCase("Jubi")) {
			// https://www.jubi.com/coin/allcoin?t=0.7755636541251603

			JSONObject json = new JSONObject(
					readJsonFromUrl("https://www.jubi.com/coin/allcoin"));

			Map<String, PoloniexMarketData> commMap = new HashMap<String, PoloniexMarketData>();
			Iterator iterator = json.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				JSONArray value = json.getJSONArray(key);

				PoloniexMarketData pmd = new PoloniexMarketData();
				pmd.setAdditionalProperty("name", value.get(0).toString()
						.toUpperCase());

				BigDecimal high = new BigDecimal(value.get(4).toString());
				BigDecimal low = new BigDecimal(value.get(5).toString());
				BigDecimal buy = new BigDecimal(value.get(1).toString());
				BigDecimal sell = new BigDecimal(value.get(3).toString());
				BigDecimal last = new BigDecimal(value.get(2).toString());
				BigDecimal vol = new BigDecimal(value.get(6).toString());
				BigDecimal qVol = new BigDecimal(value.get(7).toString());

				pmd.setBaseVolume(vol.divide(BigDecimal.valueOf(10000),
						RoundingMode.CEILING).setScale(2, RoundingMode.CEILING));
				pmd.setLast(last);
				pmd.setLowestAsk(buy);
				pmd.setHigh24hr(high);
				pmd.setLow24hr(low);
				pmd.setQuoteVolume(qVol.divide(BigDecimal.valueOf(10000),
						RoundingMode.CEILING).setScale(2, RoundingMode.CEILING));
				pmd.setHighestBid(sell);
				BigDecimal pp = (last.setScale(8, RoundingMode.CEILING)
						.subtract(low.setScale(8, RoundingMode.CEILING)))
						.divide(low.setScale(8, RoundingMode.CEILING),
								RoundingMode.CEILING);
				pmd.setPercentChange(pp);
				commMap.put(key, pmd);
			}

			return commMap;

			// return bwdsr.getTicker(currencyPair);
		} else if (exchangeName.equalsIgnoreCase("Yobit")) {
			// https://yobit.net/api/3/ticker/ltc_btc-nmc_btc
			YoBitMarketDataService bwdsr = (YoBitMarketDataService) exchange
					.getMarketDataService();
			// return bwdsr.getTicker(currencyPair);
		} else if (exchangeName.equalsIgnoreCase("Kraken")) {
			// https://api.kraken.com/0/public/Ticker
			KrakenMarketDataServiceRaw bwdsr = new KrakenMarketDataServiceRaw(
					exchange);

		}
		return new HashMap();

	}

	private static String readAll(BufferedReader rd) throws IOException {
		StringBuilder sb = new StringBuilder();

		String line = null;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();
		return sb.toString();
	}

	public static String readJsonFromUrl(String url) throws IOException,
			JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			return readAll(rd);
		} finally {
			is.close();
		}
	}
	public StatusBean smartSell(Strategy strategy){
		StatusBean bean=new StatusBean();
		bean.setFlag(-1);
		Connection conn= null;
		Statement stmt = null;
		try {
			conn= DBUtil.getInstance().getConnection();
			   stmt = conn.createStatement();
			StringBuilder beffer= new StringBuilder();
			Long time=System.currentTimeMillis();
			beffer.append("insert into strategy(strategyId,userId,platform,coinName,askOrBid,priceRule,askPrice,qty,ProfitRatio,topOrlow,strategyFlag,Price4Days,createTime) ");
			beffer.append("values('");
	        beffer.append(strategy.getStrategyId());
	        beffer.append("','");
	        beffer.append(strategy.getUserId());
	        beffer.append("','");
	        beffer.append(FunctionSet.filt(strategy.getPlatform()));
	        beffer.append("','");
	        beffer.append(FunctionSet.filt(strategy.getCoinName()));
	        beffer.append("','");
	        beffer.append(FunctionSet.filt(strategy.getAskOrBid()));
	        beffer.append("','");
	        beffer.append(FunctionSet.filt(strategy.getPriceRule()));
	        beffer.append("','");
	        beffer.append(strategy.getAskPrice());
	        beffer.append("','");
	        beffer.append(strategy.getQty());
	        beffer.append("','");
	        beffer.append(strategy.getProfitRatio());
	        beffer.append("','");
	        beffer.append(strategy.getTopOrlow());
	        beffer.append("','");
	        beffer.append(0);
	        beffer.append("','");
	        beffer.append(strategy.getPrice4Days());
	        beffer.append("',");
	        beffer.append(time);
	       beffer.append(")");
	       if(stmt.executeUpdate(beffer.toString()) > 0){
	    	   if(strategy.getStrategyFlag()==1){
	    		   String sql="SELECT * FROM strategy WHERE userId='"+strategy.getUserId()+"' and createTime='"+time+"'";
	    		   List<Strategy> strategies = DBUtil.getInstance().convert(stmt.executeQuery(sql), Strategy.class);
	    		   if(!strategies.isEmpty()){
	    			   bean.setFlag(strategies.get(0).getId());
	    		   }
	    	   }
	    	   else{
	    		   bean.setFlag(0);
	    	   }
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DBUtil.getInstance().close(stmt);
			DBUtil.getInstance().close(conn);
		}
		return bean;
	}
	
	
	//��������
	public StatusBean smartBuy(Strategy strategy){
		StatusBean bean=new StatusBean();
		bean.setFlag(0);
		Connection conn= null;
		Statement stmt = null;
		try {
			conn= DBUtil.getInstance().getConnection();
			   stmt = conn.createStatement();
			StringBuilder beffer= new StringBuilder();
			Long time=System.currentTimeMillis();
			beffer.append("insert into strategy(strategyId,userId,platform,coinName,askOrBid,priceRule,BidPrice,qty,topOrlow,strategyFlag,Price4Days,createTime) ");
			beffer.append("values('");
	        beffer.append(strategy.getStrategyId());
	        beffer.append("','");
	        beffer.append(strategy.getUserId());
	        beffer.append("','");
	        beffer.append(FunctionSet.filt(strategy.getPlatform()));
	        beffer.append("','");
	        beffer.append(FunctionSet.filt(strategy.getCoinName()));
	        beffer.append("','");
	        beffer.append(FunctionSet.filt(strategy.getAskOrBid()));
	        beffer.append("','");
	        beffer.append(FunctionSet.filt(strategy.getPriceRule()));
	        beffer.append("','");
	        beffer.append(strategy.getBidPrice());
	        beffer.append("','");
	        beffer.append(strategy.getQty());
	        beffer.append("','");
	        beffer.append(strategy.getTopOrlow());
	        beffer.append("','");
	        beffer.append(strategy.getStrategyFlag());
	        beffer.append("','");
	        beffer.append(strategy.getPrice4Days());
	        beffer.append("',");
	        beffer.append(time);
	       beffer.append(")");
	       if(stmt.executeUpdate(beffer.toString()) > 0){
	    	   bean.setFlag(1);
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DBUtil.getInstance().close(stmt);
			DBUtil.getInstance().close(conn);
		}
		return bean;
	}
	
		//取消智能买卖
		public StatusBean cancelSmartorder(String id, String userid){
			StatusBean bean=new StatusBean();
			bean.setFlag(0);
			Connection conn= null;
			Statement stmt = null;
			try {
				conn= DBUtil.getInstance().getConnection();
				stmt = conn.createStatement();
				StringBuffer sql= new StringBuffer();
				if(userid!=""||userid!=null){
				sql.append("UPDATE strategy SET strategyFlag=2 where id='"+FunctionSet.filt(id)+"' and userid='"+FunctionSet.filt(userid)+"'");
				}
				if(stmt.executeUpdate(sql.toString()) > 0){
			    	   bean.setFlag(1);
			        }
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				DBUtil.getInstance().close(stmt);
				DBUtil.getInstance().close(conn);
			}
			return bean;
		}
		
		//读取各交易所key
		
		public  PagableData<AccountBalance> getuserbalance(String platform, String userid) {
			PagableData<AccountBalance>pd=new PagableData<AccountBalance>();
			Connection conn = null;
			Statement stmt = null;
			List<AccountBalance>list=null;
			try {
				conn = DBUtil.getInstance().getConnection();
				stmt = conn.createStatement();
				String sql = "SELECT*from apis WHERE exchangid in(SELECT exchangid from exchang WHERE name='"+FunctionSet.filt(platform)+"') AND userId='"+userid+"'";
				List<Apis> ds = DBUtil.getInstance().convert(stmt.executeQuery(sql), Apis.class);
				if(!ds.isEmpty()){
					ExchangeSpecification spec = new ExchangeSpecification(class4Name(platform));
					spec.setUserName(ds.get(0).getUserid());
					spec.setApiKey(ds.get(0).getApiKey());
					spec.setSecretKey(ds.get(0).getApiSecret());
					list=getBalanceByKey(spec);
				}	
				if(!list.isEmpty()){
					pd.setDataList(list);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBUtil.getInstance().close(stmt);
				DBUtil.getInstance().close(conn);
			}			
			return pd;
		}
		
		//读取balance
		public  List<AccountBalance> getBalanceByKey(ExchangeSpecification spec){
			Map<Currency, Balance> blanceMap = null;
			Exchange bitstamp = ExchangeFactory.INSTANCE.createExchange(spec);
			AccountService accountService = bitstamp.getAccountService();
			AccountInfo accountInfo;
			try {
				accountInfo = accountService.getAccountInfo();
				blanceMap = accountInfo.getWallet().getBalances();
			} catch (NotAvailableFromExchangeException
					| NotYetImplementedForExchangeException | ExchangeException
					| IOException e) {
				e.printStackTrace();
			}
			List<AccountBalance> list=new ArrayList<AccountBalance>();
			for (Balance b : blanceMap.values()) {
				if(b.getTotal()!= null && (b.getTotal().floatValue() > 0
						|| b.getFrozen().floatValue() > 0
						|| b.getAvailable().floatValue() > 0)){
				list.add(new AccountBalance(b.getAvailable(),b.getBorrowed(),b.getDepositing(),
						b.getFrozen(),b.getTotal(),b.getWithdrawing(),b.getLoaned(),b.getCurrency().toString()));
				}		
			}
			return list;	
		}
		
		// ticker
		public  PagableData<String> getticker(String userid,String platform,String coinname) throws Exception,IOException{
			PagableData<String> pd=new PagableData<String>();
			Map<String, PoloniexMarketData> tickermap = null;
			tickermap = getAllTickerList(userid,platform);
			List<String> lastprice=new ArrayList<String>();
			if(coinname=="BCC" || coinname=="BTC"){		
				lastprice.add(null);	   
			if(platform.equals("Jubi")){
				if(tickermap.get(coinname)==null){
				}else{				
					lastprice.add(tickermap.get(coinname).getLast().toString());
				}
			} }else{ 
				lastprice.add(tickermap.get(
						"BTC_" + coinname)
						.getLast().toString());
			}	
			if(!lastprice.isEmpty()){
			pd.setDataList(lastprice);
			}
			return pd;
		}
		private  Exchange getExchangeByName(String userId, String platform)
				throws Exception {
			
			Exchange exchange = null;
			/*
			 * if (userExchangeCashedMap.containsKey(kid)) { return
			 * userExchangeCashedMap.get(kid); } else { if
			 * (UserDao.cachedApis.isEmpty()) { UserDao.loadAllApis(); }
			 */
			List<Exchang> api = loadAllApis(userId);
			if (api == null) {
				return null;
			}
			ExchangeSpecification spec = null;
			for (int i = 0; i < api.size(); i++) {
				spec = new ExchangeSpecification(class4Name(platform.toLowerCase()));
				spec.setApiKey(api.get(i).getApiKey());
				spec.setSecretKey(api.get(i).getApiSecret());

			}
			exchange = ExchangeFactory.INSTANCE.createExchange(spec);
			return exchange;
		}
		

	
}
