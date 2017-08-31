package com.bdh.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;

import com.bdh.db.entry.StatusBean;
import com.bdh.db.entry.User;
import com.bdh.db.mob.SendSMSValidCode;
import com.bdh.db.util.DBUtil;
import com.bdh.db.util.FunctionSet;

public class resetPwdDao {
	//发�?�验证码
	public StatusBean sendResetCode(String userid,String phone){
		StatusBean status = new StatusBean();
		status.setFlag(0);
		Connection conn = null;
		PreparedStatement stmt= null;
		 Random r=new Random();
	       int tag[]={0,0,0,0,0,0,0,0,0,0};
	       String four="";
	       int temp=0;
	       while(four.length()!=4){
	               temp=r.nextInt(10);//随机获取0~9的数�?
	               if(tag[temp]==0){
	                     four+=temp;
	                    tag[temp]=1;
	               }
	       }
		try {
			conn = DBUtil.getInstance().getConnection();
			phone = FunctionSet.filt(phone);
			if (SendSMSValidCode.sendSMS(phone, "【币大师】短信验证码:" + four)) {
				System.out.println("已发�?");
				StringBuilder buffer= new StringBuilder();
				buffer.append("update user set phoneValidCode='"+four+"' WHERE userId='"+userid+"' AND phone='"+phone+"'");
				stmt=conn.prepareStatement(buffer.toString());
				  if(stmt.executeUpdate() > 0){
			        	status.setFlag(1);
			        }
				status.setFlag(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.getInstance().close(stmt);
			DBUtil.getInstance().close(conn);
		}
		return status;
	}	
	//修改密码
	public StatusBean resetPwd(String userid,String code,String newPwd){
		StatusBean status = new StatusBean();
		status.setFlag(0);
		Connection conn = null;
		PreparedStatement stmt= null;
		PreparedStatement stmt2= null;
		newPwd = FunctionSet.filt(newPwd);
		newPwd = DigestUtils.md5Hex(newPwd);
		try {
			StringBuilder buffer=new StringBuilder();
	        buffer.append("select phoneValidCode from user where userid='"+FunctionSet.filt(userid)+"'");
	        conn = DBUtil.getInstance().getConnection();
			stmt = conn.prepareStatement(buffer.toString());
	        List<User> list=DBUtil.getInstance().convert(stmt.executeQuery(), User.class);
	        if (!list.isEmpty()) {
	        	 if(list.get(0).getPhoneValidCode().equals(code)){
	         		String sql2="update user set pwd='"+newPwd+"' where userId='"+FunctionSet.filt(userid)+"'";
	      	        stmt2=conn.prepareStatement(sql2.toString());
	      	       if(stmt2.executeUpdate() > 0){
	      	    	  status.setFlag(1);
	     			} 
	        	 }
			}	
	        buffer= null;
	      /*  if(stmt.executeUpdate() > 0){
	        	status.setFlag(1);
	        }*/
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DBUtil.getInstance().close(stmt);
			DBUtil.getInstance().close(conn);
		}
		return status;
	}
}
