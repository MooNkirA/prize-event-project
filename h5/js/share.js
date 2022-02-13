// "分享好友"按钮事件
$(".share-btn").click(function(){
	// 如果已锁定触摸，不允许执行
	if(typeof(touchLock) != 'undefined' && touchLock){
		return false;
	}
	
	$(".share_box").show().animate({bottom: "0px"}, 600);
	var docHeight = $(document).height(); //获取窗口高度
	   
	  $('body').append('<div id="overlay"></div>');
	  $('#overlay').show();
	  $('#overlay')
	    .height(docHeight)
	    .css({
	      'opacity': .9, //透明度
	      'position': 'absolute',
	      'top': 0,
	      'left': 0,
	      'background-color': 'rgba(0,0,0,0.5)',
	      'width': '100%',
	      'z-index': 100 //保证这个悬浮层位于其它内容之上
	
	});
	
	// 弹出分享后，禁止滑动页面
	$("body").bind("touchmove", function(event) {
		if(event.preventDefault){
			event.preventDefault();// 阻止浏览器默认事件
		}
	});
});

// "取消"按钮事件
$(".share-cancel-btn").click(function(){
	$(".share_box").animate({bottom: "-160px"}, 600, 'swing', function(){$(".share_box").hide();});
	
	// 取消分享后，解除滑动
	$("body").unbind("touchmove");
	
	$('#overlay').hide();
	   
	
	
});



$('.share_box a').each(function(){
	var href=$(this).attr('href');
	if(href.indexOf("weibo")!=-1){
		var host="http://"+window.location.host;
		var imgsrc=$('.main-pic-img').attr('src')
		if(typeof(imgsrc)=='undefined'){
			var imgsrc=$('#bg').attr('src')
		}
		if(typeof(imgsrc)=='undefined'){
			var imgsrc=$('img').eq(0).attr('src')
		}
	$(this).attr('href',href+'&pic='+host+imgsrc)
	}
})
