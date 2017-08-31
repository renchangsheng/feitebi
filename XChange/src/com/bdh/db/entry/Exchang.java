package com.bdh.db.entry;

import java.util.List;



public class Exchang extends Apis implements Comparable<Exchang>{
	
	private int id;
	private int exchangid;
	private String name;
	private String logo;
	private String status;
	private int sortBy;
	private String exchangSign;
	private String uri;
	private List<Apis> apis;


	
	public List<Apis> getApis() {
		return apis;
	}
	public void setApis(List<Apis> apis) {
		this.apis = apis;
	}
	public Exchang(int id, int exchangid, String name, String logo,
			String status, int sortBy, String exchangSign, String uri) {
		super();
		this.id = id;
		this.exchangid = exchangid;
		this.name = name;
		this.logo = logo;
		this.status = status;
		this.sortBy = sortBy;
		this.exchangSign = exchangSign;
		this.uri = uri;
	}
	public Exchang() {
		super();
	}
	public int getSortBy() {
		return sortBy;
	}
	public void setSortBy(int sortBy) {
		this.sortBy = sortBy;
	}
	public String getExchangSign() {
		return exchangSign;
	}
	public void setExchangSign(String exchangSign) {
		this.exchangSign = exchangSign;
	}
	/*public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}*/
	public int getExchangid() {
		return exchangid;
	}
	public void setExchangid(int exchangid) {
		this.exchangid = exchangid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	@Override
	public int compareTo(Exchang o) {
		// TODO Auto-generated method stub
		if(Integer.parseInt(o.getStatus())>Integer.parseInt(this.getStatus())){
		return 1;
		}
		else if(Integer.parseInt(o.getStatus())==Integer.parseInt(this.getStatus())){
			return 0;
		}
		return -1;
	}
	
	
	


}
