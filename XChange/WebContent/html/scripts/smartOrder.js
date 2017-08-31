
var userid=localStorage.getItem("userid");
$(function(){
	timedCount();
})

//显示智能合约URL
var smartUrl="http://localhost:8080/XChange/rest/strategy";
var btctoCny=0;
//显示智能合约
function showSmartDeal() {
	$.ajax({
		url:smartUrl+"/getsmartorder",
		type:'post',
		data:{userid:userid},
		dataType:'json',
		success:function(data){
			var d=data.entity.dataList;
			var sdealtext="";
			var historytext="";
			console.log(data);
			$.each(d,function(i,item){
				if(item.strategyFlag=="0"){
				sdealtext+="<tr><td>"+item.platform+"</td><td>";
				if(item.askOrBid=="Buy"){
					sdealtext+="<span class='text-success'>买</span>  "+item.coinName+"</td>";
				}
				else{
					sdealtext+="<span class='text-danger'>卖</span>  "+item.coinName+"</td>";
				}
				sdealtext+="<td name='btcNow'>0</td>";
				if(item.askOrBid=="Buy"){
					sdealtext+="<td>--</td><td>--</td>";
				}
				else{
					sdealtext+="<td>23636.3356</td><td>10%</td>";
				}
				sdealtext+="<td>"+item.priceRule+"</td><td>"+datetimeFormat_1(item.createTime)+"</td><td>" +
						"<a class='btn btn-xs btn-danger btn-outline text-danger' onclick='cancel("+item.id+")'>取消</a></td></tr>";
				}
			});
			$("#smartdealList").html(sdealtext);
			/*
			var ohistory=data.entity.dataList[0].dealhistory;
			$.each(ohistory,function(i,item){
				historytext+="<tr><td>";
				if(item.dealType=="买"){
					historytext+="<span class='text-success'>买</span></td>";
				}
				else{
					historytext+="<span class='text-danger'>卖</span></td>";
				}
				historytext+="<td>"+item.platform+"</td><td>"+item.name+"</td><td>"+item.avgPrice+"</td>" +
						"<td>"+item.dealnum+"</td><td>"+item.dealamount+"</td><td>"+item.dealtime+"</td></tr>";
				
			});
			$("#orderhistory").html(historytext);*/
		}
	});
}
//取消智能合约
function cancel(id){
	if(id!=""){
	$.ajax({
		url:smartUrl+"/cancelSmartorder",
		type:'post',
		data:{
			id:id,
			userid:userid
			},
		dataType:'json',
		success:function(data){
			if(data.entity.flag=="1"){
				showSmartDeal();
			}
		}
	});
	}
}
//格式化时间
function datetimeFormat_1(longTypeDate){ 
	  var datetimeType = ""; 
	  var date = new Date(); 
	  date.setTime(longTypeDate); 
	  datetimeType+= date.getFullYear();  //年 
	  datetimeType+= "-" + getMonth(date); //月  
	  datetimeType += "-" + getDay(date);  //日 
	  datetimeType+= "  " + getHours(date);  //时 
	  datetimeType+= ":" + getMinutes(date);   //分
	  datetimeType+= ":" + getSeconds(date);   //分
	  return datetimeType;
	} 
	//返回 01-12 的月份值  
	function getMonth(date){ 
	  var month = ""; 
	  month = date.getMonth() + 1; //getMonth()得到的月份是0-11 
	  if(month<10){ 
	    month = "0" + month; 
	  } 
	  return month; 
	} 
	//返回01-30的日期 
	function getDay(date){ 
	  var day = ""; 
	  day = date.getDate(); 
	  if(day<10){ 
	    day = "0" + day; 
	  } 
	  return day; 
	}
	//返回小时
	function getHours(date){
	  var hours = "";
	  hours = date.getHours();
	  if(hours<10){ 
	    hours = "0" + hours; 
	  } 
	  return hours; 
	}
	//返回分
	function getMinutes(date){
	  var minute = "";
	  minute = date.getMinutes();
	  if(minute<10){ 
	    minute = "0" + minute; 
	  } 
	  return minute; 
	}
	//返回秒
	function getSeconds(date){
	  var second = "";
	  second = date.getSeconds();
	  if(second<10){ 
	    second = "0" + second; 
	  } 
	  return second; 
	}
	//火币参照
	function huobi() {
		$.ajax({
			url:"http://api.huobi.com/staticmarket/detail_btc_json.js",
			dataType:'json',
			success:function(data){
				btctoCny=data.p_new;
			}
		});
		
	}
	var t;
	function timedCount()
	 {
		huobi();
		$("td[name=btcNow]").html(btctoCny);
		t=setTimeout("timedCount()",3000);
	 }