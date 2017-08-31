
var userid=localStorage.getItem("userid");
var start=0;
var limit=4;
var max=0;
var newOpenUrl="http://localhost:8080/XChange/rest/NewOpen";
var indexUrl="http://localhost:8080/XChange/rest/index";
var StrategyUrl="http://localhost:8080/XChange/rest/strategy";
var btctoCny="-";
$(function(){
	huobi();
	showNewOpen(start,limit);
	showAllApis();
	showblance();
	showSmartDeal();
	/*showmaster();*/
	Mybookmark();
	
	$("#nextPage").click(function(){
		start+=limit;
		if(start>=max){
			start=max-limit;
			if(start<=0){
				start=0;
			}
		}
		showNewOpen(start,limit);
	});
	$("#lastPage").click(function(){
		start-=limit;
		if(start<=0){
			start=0;
		}
		showNewOpen(start,limit);
		});
});
//显示新开盘的
function showNewOpen(start,limit){
			$.ajax({
				url:newOpenUrl+'/NewOpenList',
				type:"get",
				data:{
					start:start,
					limit:limit
				},
				dataType:'json',
				success:function(data){
					var d=data.entity.dataList;
					var text="";
					var rowcount=0;
					$.each(d,function(i,item){	
						text+="<tr style='height:35px;'><td>"+item.name+"</td><td>"+item.kaiPrice+"</td><td>"+item.nowPrice+"</td><td>"+item.platform+"</td><td>"+item.time+"</td></tr>";
						rowcount++;
						max=data.entity.totalCount;
			});
					if(rowcount<5){
						for(var i=0;i<5-rowcount;i++){
							text+="<tr style='height:35px;'><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>";
						}
					}	
			$("#newOpentbody").html(text);
		}
	});
}
//显示智能买卖
function showSmartDeal(){
	$.ajax({
		url:StrategyUrl+"/getsmartorders",
		type:"post",
		data:{
			userid:userid
		},
		dataType:"json",
		success:function(data){
			var d=data.entity.dataList;
			var text="";
			var s1="";
			var s2="";
			var rowccount=0;
			
			$.each(d,function(i,item){
				if(item.askOrBid=='Sell'){
					s1="卖";
					text+="<tr style='height:35px;'><td><span class='text-danger'>"+s1+"</span> "+item.coinName+"</td>" +
						"<td name='btcNow'>"+item.tickerlast+"</td>";
					if(item.priceRule==""){
						text+="<td>"+item.askPrice+"</td><td>"+item.platform+"</td>" +
					"<td>指定价格</td><td>" +
					"<a class='btn btn-xs btn-danger btn-outline text-danger' onclick='delsmart(\""+item.id+"\")' >取消</td></tr>";	
					}
					else{
						text+="<td>"+item.topOrlow+"</td><td>"+item.platform+"</td>" +
					"<td>"+item.priceRule+"</td><td>" +
					"<a class='btn btn-xs btn-danger btn-outline text-danger' onclick='delsmart(\""+item.id+"\")' >取消</td></tr>";	
					}	
				}
				else{
					s1="买";
					text+="<tr style='height:35px;'><td><span class='text-success'>"+s1+"</span> "+item.coinName+"</td>" +
					"<td name='btcNow'>"+item.tickerlast+"</td>";
				if(item.priceRule==""){
					text+="<td>"+item.bidPrice+"</td><td>"+item.platform+"</td>" +
					"<td>指定价格</td><td>" +
					"<a class='btn btn-xs btn-danger btn-outline text-danger' onclick='delsmart(\""+item.id+"\")' >取消</td></tr>";
				}
				else{
					text+="<td>"+item.topOrlow+"</td><td>"+item.platform+"</td>" +
					"<td>"+item.priceRule+"</td><td>" +
					"<a class='btn btn-xs btn-danger btn-outline text-danger' onclick='delsmart(\""+item.id+"\")' >取消</td></tr>";
				}					
				}
				rowccount++;
				});
			if(rowccount<5){
				for(var i=0;i<5-rowccount;i++){
					text+="<tr style='height:35px;'><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>";
					}
			}
			$("#stmartDeal").html(text);
			
		}
	});
	
}
var btccount=0;
var btcfrozen=0;
var cnycount=0;
var cnyfrozen=0;
var assatsCount=0;
function delsmart(id){
	$.ajax({
		url:indexUrl+"/delsmart",
		type:"post",
		data:{
			id:id
		},
		dataType:"json",
		success:function(data){
			var f=data.entity.flag;
			if(f==1){
			showSmartDeal();
			}
		}
	});
}
//账户信息
function showblance() {
	$.ajax({
		url:indexUrl+"/getAccountSummary",
		type:'get',
		data:{userid:userid},
		dataType:'json',
		success:function(data){
			var d=data.entity.dataList;	
			$.each(d,function(i,item){
				btccount=parseFloat(item.btcAount).toFixed(8);
				btcfrozen=parseFloat(item.openOrdercount).toFixed(8);
				cnycount=parseFloat(item.totlecny).toFixed(8);
				cnyfrozen=parseFloat(item.frozencny).toFixed(8);
				$("#openOrdercount").html(btcfrozen);
				$("#btcAount").html(btccount);
				$("#totelCNY").html((btccount*btctoCny)+cnycount);
				$("#availableAmount").html(((btccount-btcfrozen)*btctoCny)+(cnycount-cnyfrozen));
				$("#bdhAmount").html("3500");			
			});
			//计时器
			timedCount();
			}
	});
}
//显示交易所信息
function showAllApis(){
	$.ajax({
		url:indexUrl+"/platform_api",
		type:"get",
		data:{userId:userid},
		dataType:"json",
		success:function(data){
			var d=data.entity.dataList;
			if(d.length>0){
				var text="";
				$.each(d,function(i,item){
					text+="<div class='col-md-3 col-xs-6'>";
					text+="<div class='box' style='height:280px;'>"+
	                "<div class='box-header b-b'>"+
	                "<a class='btn btn-xs white pull-right'><i class='fa fa-chevron-right'></i></a>"+
	                "<a class='btn btn-xs white pull-right'><i class='fa fa-chevron-left'></i></a>"+
	                "<span class='h6 text-info'>"+item.logo+"</span></div>"+
	                "<div class='table-responsive "+item.logo+"'>" +
	                "<br><br><p class='h1 text-center p-t-md text-info'>" +
	                "<img  src='./logo/loging.gif'></p>"+
	                "</div></div></div>";
					Mybalance(userid,item.logo,item.apiKey,item.apiSecret);
				});
				text+="<div class='col-md-3 col-xs-6'>"+
				"<a ><div class='box white-50' style='height:280px;'>"+
	            "<br><br><br><p class='h1 text-center p-t-md text-info'>+</p></div></a></div>";
				$("#mapBlance").html(text);
			}
		}
		
	});
}
//显示我的balance信息
function Mybalance(userid,platform,key,Secret){
	if(userid!=""&&platform!=""&&key!=""&&Secret!=""){
		$.ajax({
			url:indexUrl+"/platform_getPlatformSummary",
			type:"get",
			data:{userId:userid,
				  platformindex:platform,
				  apikey:key,
				  apisecret:Secret
				},
			dataType:"json",
			success:function(data){
				var d=data.entity;
				var rowccount=0;
				console.log(data);
				$.each(d,function(i,item){
					var json=item;
					var text="<table class='table table-sm text-sm'><thead>"+
	                "<tr style='white-space:nowrap'>"+
	                "<th>币种</th>"+
	                "<th>总数量</th>"+
	                "<th>可用量</th></tr></thead>"+
	                "<tbody class='text-xs'>"; 
					$.each(json,function(j,iten){
						text+="<tr style='height:30px;'><td>"+iten.currency+"</td><td>"+parseFloat(iten.total).toFixed(8)+"</td><td>"+parseFloat(iten.available).toFixed(8)+"</td></tr>";
						assatsCount++;
						rowccount++;
					});
					if(rowccount<5){
						for(var k=0;k<5-rowccount;k++){
							text+="<tr style='height:30px;'><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>";
						}
					}
					text+="</tbody></table></div></div></div>";
				$("div ."+i+"").html(text);
				});
				$("#assatsCount").html(assatsCount);
			},
				error:function(e){
					$("div ."+i+"").html("<br><br><br><p class='h1 text-center p-t-md text-info'>加载超时，请重试</p>");
				}
		});
	}
}



//我关注的币
function Mybookmark(){
	$.ajax({
		url:indexUrl+"/MyBookmark",
		type:"post",
		data:{userid:userid},
		dataType:"json",
		success:function(data){
		
			var bookmark=data.entity.dataList;
			var bookmarkText="";
			var bookmarkcount=0;
			$.each(bookmark,function(b,item){
				bookmarkText+="<tr style='height:35px;'><td>"+item.currency+"</td><td>"+item.lastPrice+"</td><td>"+item.hightPrive+"</td>" +
						"<td>"+item.lowPrice+"</td><td>"+item.volumeCount+"</td><td>"+item.platform+"</td></tr>";	
				bookmarkcount++;
			});
			if(bookmarkcount<5){
				for(var i=0;i<5-bookmarkcount;i++)
				bookmarkText+="<tr style='height:35px;'><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>";	
			}
			$("#bookList").html(bookmarkText);
			
		}
	});
}
//关注的大师
function showmaster() {
	$.ajax({
		url:indexUrl+"/getMymaster",
		type:'get',
		data:{userid:userid},
		dataType:'json',
		success:function(data){
			var master=data.entity;
			var masterText="";
			var rowccount=0;
			$.each(master,function(k,item){
				
				var btccny=parseFloat(item.asset).toFixed(8)*parseFloat(btctoCny).toFixed(8);
				var cny=parseFloat(item.assetCNY).toFixed(8);
				var zichan=parseFloat(parseFloat(btccny).toFixed(8)+parseFloat(cny).toFixed(8)).toFixed(8);
				masterText+="<tr style='height:35px;'><td>"+item.address+"</td><td>"+zichan+"</td><td>"+item.fansnumber+"</td><td>"+item.tactile+"</td><td>"+item.dayincreases+"</td></tr>";
				rowccount++;
			});
			if(rowccount<5){
				for(var i=0;i<5-rowccount;i++)
				masterText+="<tr style='height:35px;'><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>";
			}
			$("#masterList").html(masterText);	
		}
	});
	
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
	Mybookmark();
	showSmartDeal();
	$("#availableAmount").html(parseFloat(((btccount-btcfrozen)*btctoCny)+(cnycount-cnyfrozen)).toFixed(8));
	$("#totelCNY").html(parseFloat((btccount*btctoCny)+cnycount).toFixed(8));
	t=setTimeout("timedCount()",5000);
 }