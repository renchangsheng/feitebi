package com.bdh.db.entry;

public class event {

	private String id;
	private String title;
	private String contents;
	private String TargetTime;
	private int flag;
	private int createTime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		this.contents = contents;
	}
	public String getTargetTime() {
		return TargetTime;
	}
	public void setTargetTime(String targetTime) {
		TargetTime = targetTime;
	}
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public int getCreateTime() {
		return createTime;
	}
	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}
	public int getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(int updateTime) {
		this.updateTime = updateTime;
	}
	private int updateTime;
	public event(String id, String title, String contents, String targetTime,
			int flag, int createTime, int updateTime) {
		super();
		this.id = id;
		this.title = title;
		this.contents = contents;
		TargetTime = targetTime;
		this.flag = flag;
		this.createTime = createTime;
		this.updateTime = updateTime;
	}
	public event() {
		super();
		// TODO Auto-generated constructor stub
	}

}
