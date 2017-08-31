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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.json.JSONException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.h2.constant.SysProperties;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.bittrex.v1.dto.marketdata.BittrexTicker;
import org.knowm.xchange.bittrex.v1.service.BittrexMarketDataServiceRaw;
import org.knowm.xchange.bleutrade.dto.marketdata.BleutradeTicker;
import org.knowm.xchange.bleutrade.service.BleutradeMarketDataServiceRaw;
import org.knowm.xchange.btc38.dto.marketdata.Btc38Ticker;
import org.knowm.xchange.btc38.dto.marketdata.Btc38TickerReturn;
import org.knowm.xchange.btce.v3.service.BTCEMarketDataServiceRaw;
import org.knowm.xchange.btctrade.service.BTCTradeMarketDataService;
import org.knowm.xchange.bter.dto.marketdata.BTERTicker;
import org.knowm.xchange.bter.service.BTERMarketDataServiceRaw;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.hitbtc.dto.marketdata.HitbtcTicker;
import org.knowm.xchange.hitbtc.service.HitbtcMarketDataService;
import org.knowm.xchange.huobi.service.HuobiMarketDataService;
import org.knowm.xchange.kraken.service.KrakenMarketDataServiceRaw;
import org.knowm.xchange.livecoin.LivecoinExchange;
import org.knowm.xchange.poloniex.dto.marketdata.PoloniexMarketData;
import org.knowm.xchange.poloniex.service.PoloniexMarketDataServiceRaw;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.yobit.service.YoBitMarketDataService;

import com.bdh.db.entry.Account;
import com.bdh.db.entry.AccountBalance;
import com.bdh.db.entry.Apis;
import com.bdh.db.entry.Bookmark;
import com.bdh.db.entry.Exchang;
import com.bdh.db.entry.Following;
import com.bdh.db.entry.Keysecret;
import com.bdh.db.entry.PagableData;
import com.bdh.db.entry.indexMyfollowing;
import com.bdh.db.util.DBUtil;
import com.bdh.db.util.FunctionSet;
import com.bdh.db.view.Fansnumber;

public class BalanceDao {


	public static final Map<String, Exchange> userExchangeCashedMap = new ConcurrentHashMap<String, Exchange>();
	
	public  Exchange getExchangeByNames(String userId,String platform) throws Exception {
		String kid = userId + "_" +platform;
		Exchange exchange = null;
		if (userExchangeCashedMap.containsKey(kid)) {
			return userExchangeCashedMap.get(kid);
		} else {
			if (UserDao.cachedApis.isEmpty()) {
				UserDao.loadAllApis();
			}
			Exchang api = UserDao.cachedApi.get(kid);
			if (api == null) {
				return null;
			}
			ExchangeSpecification spec = new ExchangeSpecification(class4Name(platform));
			spec.setApiKey(api.getApiKey());
			spec.setSecretKey(api.getApiSecret());
			exchange = ExchangeFactory.INSTANCE.createExchange(spec);
			userExchangeCashedMap.put(kid, exchange);
		}
		return exchange;
	}
	
	public PagableData<Exchang> queryApis(String userid) {
		Connection conn = null;
		Statement stmt = null;
		PagableData<Exchang>pd= new PagableData<Exchang>();
		try {
			conn = DBUtil.getInstance().getConnection();
			stmt = conn.createStatement();
			String sql = "SELECT exchang.logo,apis.apiKey,apis.apiSecret FROM user,apis,exchang where user.userId=apis.userId "
					+ "and apis.isEnable=1 and apis.flag=1 AND apis.exchangid=exchang.exchangid and user.userId='"
					+ FunctionSet.filt(userid) + "'";
			List<Exchang> ds = DBUtil.getInstance().convert(
					stmt.executeQuery(sql), Exchang.class);
			if(!ds.isEmpty()){
				pd.setDataList(ds);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.getInstance().close(stmt);
			DBUtil.getInstance().close(conn);
		}
		return pd;
	}
	
	public static void main(String[] args) throws Exception, IOException{
		BalanceDao dao= new BalanceDao();
	    dao.getAllPlatformSummary("103417c8d47843aca10b2ed18ae39d4d","bittrex","9891c700b5e64f1ea410e9b52da309ee","6690e214f87f4594a02cb5a1a96ec052");
	}
/***
*数据
*/
public Map<String, Collection<AccountBalance>> getAllPlatformSummary(
			String userId ,String platformindex,String apikey,String apisecret) throws Exception, IOException {
		Map<String, Collection<AccountBalance>> allPlatformBalanceMap = new LinkedHashMap<String, Collection<AccountBalance>>();
		ExchangeSpecification spec = new ExchangeSpecification(class4Name(platformindex));
		spec.setApiKey(apikey);
		spec.setSecretKey(apisecret);
		Exchange exchange = ExchangeFactory.INSTANCE.createExchange(spec);
				Collection<AccountBalance> balanceList = new ArrayList<AccountBalance>();
				Map<Currency, Balance> blanceMap = exchange.getAccountService()
						.getAccountInfo().getWallet().getBalances();
				for (Balance b : blanceMap.values()) {
					if (b.getTotal().floatValue() > 0) {
						AccountBalance ab = new AccountBalance();
						ab.setAvailable(b.getAvailable());
						ab.setBorrowed(b.getBorrowed());
						ab.setDepositing(b.getDepositing());
						ab.setFrozen(b.getFrozen());
						ab.setTotal(b.getTotal());
						ab.setWithdrawing(b.getWithdrawing());
						ab.setLoaned(b.getLoaned());
						ab.setCurrency(b.getCurrency().getCurrencyCode());
						balanceList.add(ab);
					}
				}
				if (!balanceList.isEmpty()) {
					allPlatformBalanceMap.put(platformindex, balanceList);
					
					
				}

		return allPlatformBalanceMap;
	}
	
	
	
	/**
	 * ��ѯ�û�keyֵ
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

	/***
	 * 首页我关注的币
	 * */
	public PagableData<Bookmark> MyBookmark(String userid) {
		Connection conn = null;
		Statement stmt = null;
		PagableData<Bookmark> pd = new PagableData<Bookmark>();
		try {
			conn = DBUtil.getInstance().getConnection();
			stmt = conn.createStatement();
			String sql = "SELECT * from bookmark where flag=1 and userid='"
					+ FunctionSet.filt(userid) + "'";
			List<Bookmark> ds = DBUtil.getInstance().convert(
					stmt.executeQuery(sql), Bookmark.class);
			if (!ds.isEmpty()) {
				Map<String, PoloniexMarketData> tickermap = null;
				for (int i = 0; i < ds.size(); i++) {
					tickermap = getAllTickerList(userid, ds.get(i)
							.getPlatform().substring(0, 1).toUpperCase()
							+ ds.get(i).getPlatform().substring(1));
					for (String key : tickermap.keySet()) {
						BigDecimal lastPrice = BigDecimal.ZERO;
						BigDecimal hightPrive = BigDecimal.ZERO;
						BigDecimal lowPrice = BigDecimal.ZERO;
						BigDecimal volumeCount = BigDecimal.ZERO;
						if (ds.get(i).getPlatform().equals("jubi")) {
							if (key.equals(ds.get(i).getCurrency()
									.toLowerCase())) {
								lastPrice = tickermap.get(key).getLast();
								hightPrive = tickermap.get(key).getHighestBid();
								lowPrice = tickermap.get(key).getLowestAsk();
								volumeCount = tickermap.get(key).getQuoteVolume();
								ds.get(i).setLastPrice(lastPrice.toString());
								ds.get(i).setHightPrive(hightPrive.toString());
								ds.get(i).setLowPrice(lowPrice.toString());
								ds.get(i).setVolumeCount(volumeCount.toString());

							}
						}
						if (key.equals("BTC_"
								+ ds.get(i).getCurrency().toUpperCase())) {

							lastPrice = tickermap.get(key).getLast();
							hightPrive = tickermap.get(key).getHighestBid();
							lowPrice = tickermap.get(key).getLowestAsk();
							volumeCount = tickermap.get(key).getQuoteVolume();
							ds.get(i).setLastPrice(lastPrice.toString());
							ds.get(i).setHightPrive(hightPrive.toString());
							ds.get(i).setLowPrice(lowPrice.toString());
							ds.get(i).setVolumeCount(volumeCount.toString());

						} else {
							lastPrice = tickermap.get(key).getLast();
							hightPrive = tickermap.get(key).getHighestBid();
							lowPrice = tickermap.get(key).getLowestAsk();
							volumeCount = tickermap.get(key).getQuoteVolume();
						}
					}
				}
				pd.setDataList(ds);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DBUtil.getInstance().close(stmt);
			DBUtil.getInstance().close(conn);
		}
		return pd;
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

	private Exchange getExchangeByName(String userId, String platform)
			throws Exception {
		String kid = userId + "_" + platform;
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
	/*	userExchangeCashedMap.put(kid, exchange);*/
		return exchange;
	}

	public List<LimitOrder> getMyOpenOrderList(String userId, String platform)
			throws Exception {
		Exchange exchange = getExchangeByName(userId, platform);
		if (exchange == null) {
			return new ArrayList<>();
		}
		OpenOrders uts = exchange.getTradeService().getOpenOrders();
		/* OpenOrders uts1 = exchange.getTradeService().getTradeHistory(arg0); */
		return uts.getOpenOrders();
	}

	
	// 我关注的大师关注量
		public PagableData<Fansnumber> fansnumber(String userid) {
			Connection conn = null;
			Statement stmt = null;
			PagableData<Fansnumber>pd= new PagableData<Fansnumber>();
			try {
				conn = DBUtil.getInstance().getConnection();
				stmt = conn.createStatement();
				String sql = "SELECT COUNT(strategy.strategyId) as strategynum,strategy.userid as userid,user.walletAddr as walletAddr,fansnumber.fansnumber as fansnumber"
						+ " FROM strategy,user,fansnumber WHERE strategy.strategyId='0' and fansnumber.followeduserid=strategy.userId and "
						+ " user.userId=strategy.userId and strategy.userId in(SELECT following.followedUserId from following WHERE "
						+ " following.userId='"+FunctionSet.filt(userid)+"') GROUP BY strategy.userId";
				List<Fansnumber> fansnumberlist = DBUtil.getInstance().convert(
						stmt.executeQuery(sql), Fansnumber.class);
				pd.setDataList(fansnumberlist);
			
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBUtil.getInstance().close(stmt);
				DBUtil.getInstance().close(conn);
			}
			return pd;
		}

	/***
	 * 我关注的大师apis
	 * */
	public List<Exchang> myfollowApis(String userid) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DBUtil.getInstance().getConnection();
			stmt = conn.createStatement();
			String sql = "SELECT * FROM apis,exchang where isEnable=1 and flag=1 and  apis.exchangid=exchang.exchangid and userId "
					+ " in(SELECT followedUserId from following WHERE userId='"
					+ FunctionSet.filt(userid) + "');";
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

	/**
	 * 关注的大师
	 * */
	public List<indexMyfollowing> getMymaster(String userid)throws Exception {
		List<indexMyfollowing> followinglist= new ArrayList<indexMyfollowing>();
		indexMyfollowing following= new indexMyfollowing();
		Map<String, List<indexMyfollowing>> map = new LinkedHashMap<String, List<indexMyfollowing>>();
		List<Exchang> list = myfollowApis(userid);
		Map<Currency, Balance> blanceMap = null;
		Map<String, PoloniexMarketData> tickermap = null;
		BigDecimal btc = BigDecimal.ZERO;
		BigDecimal convertiblebtc = BigDecimal.ZERO;
		BigDecimal sumbtc = BigDecimal.ZERO;
		BigDecimal last = BigDecimal.ZERO;
		BigDecimal sumcny = BigDecimal.ZERO;
		String[] a = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ExchangeSpecification spec = new ExchangeSpecification(
					class4Name(list.get(i).getLogo()));
			spec.setUserName(userid);
			spec.setApiKey(list.get(i).getApiKey());
			spec.setSecretKey(list.get(i).getApiSecret());
			a[i] = list.get(i).getLogo();
			Exchange bitstamp = ExchangeFactory.INSTANCE.createExchange(spec);
			AccountService accountService = bitstamp.getAccountService();
			AccountInfo accountInfo = accountService.getAccountInfo();
			blanceMap = accountInfo.getWallet().getBalances();

			tickermap = getAllTickerList(userid, list.get(i).getLogo()
					.substring(0, 1).toUpperCase()
					+ list.get(i).getLogo().substring(1));
			List<indexMyfollowing> followlist = new ArrayList<indexMyfollowing>();
			for (Balance b : blanceMap.values()) {
				if (b.getCurrency().getCurrencyCode().equals("BTC")) {
					btc = b.getTotal();
				} else {
					if (b.getTotal() != null
							&& (b.getTotal().floatValue() > 0
									|| b.getFrozen().floatValue() > 0 || b
									.getAvailable().floatValue() > 0)) {

						if (a[i].equals("jubi") || a[i].equals("bittrex")) {
							if (tickermap
									.get(b.getCurrency().getCurrencyCode()) == null) {

							} else {
								last = tickermap.get(
										b.getCurrency().getCurrencyCode())
										.getLast();
							}
						} else if (b.getCurrency().equals("BCC")) {
							last = tickermap.get(
									"BTC_" + b.getCurrency().getCurrencyCode())
									.getLast();
						} else {
							last = tickermap.get(
									"BTC_" + b.getCurrency().getCurrencyCode())
									.getLast();
						}

					}
					convertiblebtc = b.getTotal().multiply(last);
				}
				if (b.getCurrency().getCurrencyCode().equals("CNY")) {
					sumcny = b.getTotal();
				}
			}
			sumbtc = convertiblebtc.add(btc);
		}
		PagableData<Fansnumber> Fansnumber=fansnumber(userid);
		followinglist.add(following);
		following.setFansnumber(String.valueOf(Fansnumber.getDataList()));
		following.setAsset(sumbtc.toString());
		following.setAccetCNY(sumcny.toString());
		return followinglist;
	}

/*	public List<Following> Mymaster(String userid) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DBUtil.getInstance().getConnection();
			stmt = conn.createStatement();
			String sql = "SELECT * from following,user where user.userId=following.followedUserId  AND following.flag=1 and following.userId='"
					+ FunctionSet.filt(userid) + "'";
			List<Following> ds = DBUtil.getInstance().convert(
					stmt.executeQuery(sql), Following.class);
			if (!ds.isEmpty()) {
				List<Fansnumber> fansnumberlist = fansnumber(userid);
				for (int i = 0; i < ds.size(); i++) {
					for (int j = 0; j < fansnumberlist.size(); j++) {
						if (fansnumberlist.get(j).getFolloweduserid()
								.equals(ds.get(i).getUserId())) {
							String Fansnumber = fansnumberlist.get(j)
									.getFansnumber();
							ds.get(i).setFansnumber(Fansnumber);
						}
					}
				}
			}
			return ds;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.getInstance().close(stmt);
			DBUtil.getInstance().close(conn);
		}
		return new ArrayList<Following>(0);
	}*/

	// index 数据
	public PagableData<Account> getAccountSummary(String userid)
			throws Exception, IOException {
		PagableData<Account> acc = new PagableData<Account>();
		Account account = new Account();
		List<Account> acccc = new ArrayList<Account>();
		Map<String, List<AccountBalance>> allPlatformBalanceMap = new LinkedHashMap<String, List<AccountBalance>>();
		List<Exchang> list = loadAllApis(userid);
		Map<Currency, Balance> blanceMap = null;
		String[] a = new String[list.size()];
		Map<String, PoloniexMarketData> tickermap = null;
		int assatsCount = 0;
		int coinnum = 0;
		int btcsice=0;
		int cnysice=0;
		BigDecimal frozentotal = BigDecimal.ZERO;
		BigDecimal btctotal = BigDecimal.ZERO;
		BigDecimal btccount = BigDecimal.ZERO;
		BigDecimal trunbtc = BigDecimal.ZERO;
		BigDecimal frozenBTC = BigDecimal.ZERO;
		BigDecimal frozentrunbtc = BigDecimal.ZERO;
		BigDecimal totlecny = BigDecimal.ZERO;
		BigDecimal Frozencny = BigDecimal.ZERO;
		try {
		for (int i = 0; i < list.size(); i++) {
			
				ExchangeSpecification spec = new ExchangeSpecification(
						class4Name(list.get(i).getLogo()));
				spec.setUserName(userid);
				spec.setApiKey(list.get(i).getApiKey());
				spec.setSecretKey(list.get(i).getApiSecret());
				a[i] = list.get(i).getLogo();
				Exchange bitstamp = ExchangeFactory.INSTANCE
						.createExchange(spec);
				AccountService accountService = bitstamp.getAccountService();
				AccountInfo accountInfo = accountService.getAccountInfo();
				blanceMap = accountInfo.getWallet().getBalances();
				tickermap = getAllTickerList(userid, list.get(i).getLogo()
						.substring(0, 1).toUpperCase()
						+ list.get(i).getLogo().substring(1));
				BigDecimal last = BigDecimal.ZERO;
				List<AccountBalance> balanceList = new ArrayList<AccountBalance>();

				for (Balance b : blanceMap.values()) {
					
					if (b.getCurrency().getCurrencyCode().equals("BTC")) {
						btcsice=1;
						btccount = b.getTotal();
						frozenBTC = frozenBTC.add(b.getFrozen());
					} else {
						if (b.getTotal() != null
								&& (b.getTotal().floatValue() > 0
										|| b.getFrozen().floatValue() > 0 || b
										.getAvailable().floatValue() > 0)) {
							if (a[i].equals("jubi") || a[i].equals("bittrex")) {
								if (tickermap.get(b.getCurrency()
										.getCurrencyCode()) == null) {

								} else {
									last = tickermap.get(
											b.getCurrency().getCurrencyCode())
											.getLast();
								}
							} else if (b.getCurrency().equals("BCC")) {
								last = tickermap.get(
										"BTC_"
												+ b.getCurrency()
														.getCurrencyCode())
										.getLast();
							} else {
								last = tickermap.get(
										"BTC_"
												+ b.getCurrency()
														.getCurrencyCode())
										.getLast();
							}

							AccountBalance ab = new AccountBalance();
							ab.setAvailable(b.getAvailable());
							ab.setBorrowed(b.getBorrowed());
							ab.setDepositing(b.getDepositing());
							ab.setFrozen(b.getFrozen());
							ab.setTotal(b.getTotal());
							ab.setWithdrawing(b.getWithdrawing());
							ab.setLoaned(b.getLoaned());
							ab.setCurrency(b.getCurrency().getCurrencyCode());
							
							balanceList.add(ab);
							assatsCount = balanceList.size();
					
							
						}
						trunbtc = trunbtc.add(b.getTotal().multiply(last));
						frozentrunbtc = frozentrunbtc.add(b.getFrozen().multiply(last))
								.setScale(4, BigDecimal.ROUND_HALF_UP);
					}

					if (b.getCurrency().getCurrencyCode().equals("CNY")) {
						cnysice=1;
						totlecny = b.getTotal();
						Frozencny = b.getFrozen();
					}

				}
				allPlatformBalanceMap.put(a[i], balanceList);
		/*		coinnum += assatsCount+btcsice+cnysice;*/
				
				btctotal = trunbtc.add(btccount);
				frozentotal = frozentrunbtc.add(frozenBTC);
				account.setTotlecny(totlecny);
				account.setFrozencny(Frozencny);
			/*	account.setAssatsCount(String.valueOf(coinnum));*/
				account.setOpenOrdercount(String.valueOf(frozentotal));
				account.setBtcAount(String.valueOf(btctotal));// btc
				account.setList(allPlatformBalanceMap);
			}
				acccc.add(account);
				acc.setDataList(acccc);			
				return acc;
			} catch (Exception e) {
				e.printStackTrace();
				return new PagableData<Account>();		
			}
	}

	public Map<String, Collection<LimitOrder>> getMyAllOpenOrderList(
			String userId) throws Exception {
		Map<String, Collection<LimitOrder>> allPlatformOrderMap = new LinkedHashMap<String, Collection<LimitOrder>>();
		Set<String> keys = userExchangeCashedMap.keySet();
		for (String key : keys) {
			if (key.indexOf(userId + "_") >= 0) {

				String platform = key.split("_")[1];
				Exchange exchange = getExchangeByName(userId, platform);
				if (exchange == null) {
					return allPlatformOrderMap;
				}
				OpenOrders uts = exchange.getTradeService().getOpenOrders();
				allPlatformOrderMap.put(platform, uts.getOpenOrders());
			}
		}
		return allPlatformOrderMap;
	}

	// ticker
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

			/*
			 * Btc38AllTicker btc38 = new Btc38AllTicker(exchange);
			 * System.out.println(1111111111); Map<String, PoloniexMarketData>
			 * commMap = new HashMap<String, PoloniexMarketData>(); Map<String,
			 * Btc38TickerReturn> tikers = btc38.getBtc38AllTickers("CNY"); for
			 * (String k : tikers.keySet()) { Btc38Ticker t =
			 * tikers.get(k).getTicker(); PoloniexMarketData pmd = new
			 * PoloniexMarketData();
			 * 
			 * pmd.setBaseVolume(t.getVol()); pmd.setLast(t.getLast());
			 * pmd.setLowestAsk(t.getSell()); pmd.setHigh24hr(t.getHigh());
			 * pmd.setLow24hr(t.getLow()); pmd.setQuoteVolume(t.getVol());
			 * pmd.setHighestBid(t.getHigh()); if (t.getLast() != null &&
			 * t.getLast().doubleValue() > 0) { BigDecimal pp =
			 * (t.getLast().setScale(8, RoundingMode.CEILING)
			 * .subtract(t.getLow().setScale(8, RoundingMode.CEILING)))
			 * .divide(t.getLow().setScale(8, RoundingMode.CEILING),
			 * RoundingMode.CEILING); pmd.setPercentChange(pp); }
			 * commMap.put(k.toString(), pmd); }
			 * 
			 * return commMap;
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
				/*
				 * BigDecimal pp = (last.setScale(8, RoundingMode.CEILING)
				 * .subtract(low.setScale(8, RoundingMode.CEILING)))
				 * .divide(low.setScale(8, RoundingMode.CEILING),
				 * RoundingMode.CEILING); System.out.println(pp);
				 * pmd.setPercentChange(pp);
				 */
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

}
