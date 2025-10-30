package net.ddsmedia.tusa.tusamovil.Utils;

public class NavDrawerItem {
	
	private String title;
	private int icon;
	private String count = "0";
	// boolean to set visiblity of the counter
	private boolean isCounterVisible = false;

	public Integer getMnu_option() {
		return mnu_option;
	}

	public void setMnu_option(Integer mnu_option) {
		this.mnu_option = mnu_option;
	}

	private Integer mnu_option;
	
	public NavDrawerItem(){}

	public NavDrawerItem(String title, int icon, Integer mnu_option){
		this.title = title;
		this.icon = icon;
		this.mnu_option = mnu_option;
	}
	
	public NavDrawerItem(String title, int icon, boolean isCounterVisible, String count, Integer mnu_option){
		this.title = title;
		this.icon = icon;
		this.isCounterVisible = isCounterVisible;
		this.count = count;
		this.mnu_option = mnu_option;
	}
	
	public String getTitle(){
		return this.title;
	}
	
	public int getIcon(){
		return this.icon;
	}
	
	public String getCount(){
		return this.count;
	}
	
	public boolean getCounterVisibility(){
		return this.isCounterVisible;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public void setIcon(int icon){
		this.icon = icon;
	}
	
	public void setCount(String count){
		this.count = count;
	}
	
	public void setCounterVisibility(boolean isCounterVisible){
		this.isCounterVisible = isCounterVisible;
	}
}
