package com.bdh.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import com.bdh.db.entry.Exchang;
import com.bdh.db.entry.PagableData;
import com.bdh.db.util.DBUtil;



public class ExchangDao {
	
	
	/**
	 * 交易所动态显示显示
	 * */
	public PagableData<Exchang> getExchang(){
		Connection conn = null;
		PreparedStatement stmt= null;
		PagableData<Exchang> pd=new PagableData<Exchang>();
		try {
			
			StringBuilder sql= new StringBuilder();
			sql.append("SELECT * FROM exchang");
			conn= DBUtil.getInstance().getConnection();
			stmt=conn.prepareStatement(sql.toString());
			List<Exchang> list=DBUtil.getInstance().convert(stmt.executeQuery(), Exchang.class);
			
		    if(!list.isEmpty()){
		    	pd.setDataList(list);
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
	public static void main(String[] args) {
		ExchangDao dao =new ExchangDao();
		List<Exchang> list= (List<Exchang>) dao.getExchang().getDataList();
		for (Exchang exchang : list) {
			System.out.println("=="+exchang.getName());
		}
	}
	

}
