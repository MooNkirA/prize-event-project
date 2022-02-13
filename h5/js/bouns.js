//抽中的奖品，用于最后显示
var prizes = [];
//倒计时秒
var endtime=1;
//活动名称
var gamename = '';
//cookie判断倒计时动画信息，第一次加载，后续不再加载
var motion={};

(function(){
	//获取活动信息，计算倒计时，必须同步
	$.ajax({
		url:'/api/game/info/'+getParam('id'),
		async: false,
		success: function (d) {
			if(d.code == 1){
				endtime = parseInt((new Date(d.data.endtime) - new Date(d.now))/1000);
				gamename = d.data.title;
			}else{
				alert(d.msg);
			}
        }
	});
	//用户信息，必须同步
    $.ajax({
        url:'/api/user/info',
        async: false,
        success: function (d) {
            if(d.code == 1){
            	//以用户id-活动id做key，判断cookie有没有写入，如果没有，展示倒计时动画
                var key = d.data.id + '-' + getParam('id');
                if(getCookie(key) == null){
                	motion = {"readyTime":3,"txt":"GO","time":1000,"first":true};
                	setCookie(key,1,"d30");
				}else{
                    motion = {"readyTime":0,"txt":"","time":0,first:false};
				}
            }else{
                alert(d.msg);
                window.location.href='/index.html';
            }
        }
    });
	
	var Bouns = function(options){
		return this._init(options||{});
	}
	Bouns.prototype = {
		_init : function(options){
			this.ops = $.extend({
				unit : '' ,
				min:1 ,
				max:10 ,
				elem : 'body' ,
				time : 1 , //单位秒
				intervalTime : 400 ,
				readyTime : motion.readyTime, //单位秒
				timeout : null ,
			},options);
			this.money = 0 ;
			this.timer = null ;
			this.timeoutTimer = null ;
			this.readyTimer = null ;
			this._bindEvent();
			return this ;
		},
		_createText : function(){
			$(this.ops.elem).append('<div id="bonus_show" class="showTxt bonus_show"><span class="bonus_price" style="color:#ffffff"></span><span class="bonus_unit">'+this.ops.unit+'</span></div>');
			this.bonus_show = $(".bonus_show").last() ;
			this.bonus_price = $(".bonus_price").last() ;
			this.bonus_unit = $(".bonus_price").last() ;
		},
		//创建价格
		_textShow : function(x,y){
			this._createText() ;
			var _this = this ;
			// var price = this._getRandom();
			this.money += 1 ;
            // this.bonus_price.text(price);
            var text = '';
            $.ajax({
                url:'/api/act/go/'+getParam('id'),
                async: false,
                success:function (d) {
                    if(d.code != 1){
                        text = d.msg;
                    }else{
                        text = d.data.name;
                        prizes.push(d.data.name);
                    }
                }
            });
            //奖品名称
            this.bonus_price.text(text);
            this.bonus_show.css({left : x ,top:y}).addClass("on") ;
		},
		
		_bindEvent : function(){
			
			var _this = this ;
			$(this.ops.elem).on("touchmove",function(e){
				e.preventDefault() ;
				e.stopPropagation() ;
			});
			
			//点击红包操作
			$("body").on("click",".bonusList",function(e){
				
				var offset = $(this).offset() ;
				//click 抽奖
				_this._textShow(offset.left,offset.top);
				$(this).remove();
			});
		},
		
		
		_getRandom : function(){
			var random = Math.random() ;
			random == 0 ? 1 : random ;//如果随机数是0   运气爆棚，直接给个最大奖
			var val = random * this.ops.max ;
			val = val.toFixed(1) ;
			return parseFloat(val) ;
		},
		
		
		_createItems : function(){
			var _this = this ;
			var left = $(window).width() * Math.random() - 60;
			left = left<0 ? 0 : left > 750 ? 750 : left ;
			var angle = Math.random() * 90 - 45 ;
			var scale = (Math.random()*40 + 60) / 100 ;
			scale = scale.toFixed(1) ;
			var transformStyle = "-webkit-transform:rotate("+angle+"deg) scale("+scale+");" ;
				transformStyle +="transform:rotate("+angle+"deg) scale("+scale+");";
			var itemHtml = '<a class="bonusList" style="left:'+left+'px;  '+transformStyle+'"  href="javascript:;" ></a>' ;
		
			$(this.ops.elem).append(itemHtml);
			
			setTimeout(function(){
				$(_this.ops.elem).find(".bonusList").last().addClass("down");
			},50);
		},
		//返回物品列表
		getMoney:function(){
			return this.money / 100 ;
		},
		
		stop : function(){
			clearTimeout(this.timer) ;
			this.timer = null ;
			$(this.ops.elem).find(".bonusList").remove();
			if(this.timeoutBox ) this.timeoutBox.remove();
			clearInterval(this.timeoutTimer) ;
			this.timeoutTimer = null ;
		},
		
		//剩余时间倒计时
		_createTimeout:function(){
			
			//var html = '<div class="timeoutBox" id="timeoutBox"></div>' ;

					
			var html = '<div class="timeoutBox">'+
						'<span style="color:white">剩余时间&nbsp;&nbsp;</span>'+
						'<progress value="'+endtime+'" max="'+endtime+'" class="gress"></progress>'+
						//'<span id="proBox"></span>'+
						//'<div class="rain-wrap hide">'+
						 // '<div class="over_date">倒计时 <span class="date_time">30</span> 秒</div>'+
						//'</div>'+
						'&nbsp;&nbsp;<span class="z1" style="color:white"></span>'+
						'</div>';
			
		
			$(this.ops.elem).append(html);
			//this.timeoutBox = $("#loader") ;
		},
		//创建nav
		_createNav:function(){
			var html = '<div class="nav"  style="margin-top:-6px">'+
						 '<div class="img-close">'+
						 '<a href="index.html">'+
						 '<img style="width:44px;height:44px;" src="./img/btn_nav_back@3x.png" onclick="javascript:location.href=index.html" />'+
						 '</a>'+
						 '</div>'+
						 '<div class="intro">'+ gamename +
						 '</div>'+
						 '<div class="img-share">'+
						 '<a href="#" onclick="showList3()">'+
						 '<img style="width:44px;height:44px;" src="./img/btn_nav_share@3x.png" />'+
						 '</a>'+
						 '</div>'
						'</div>';
			$(this.ops.elem).append(html);
		},
		//创建底部背景图片
		_createFooter:function(){
			var html = '<div><div class="imme_moon"><img src="img/img_bg_moon.png" /></div><div class="hdjs"><a href="activity-rule.html?id='+getParam('id')+'"><img src="img/img_huodongguize@3x.png" /></a></div></div>' ;
			$(this.ops.elem).append(html);
		},
		
		//创建我的奖品区域
		_createAward:function(){
			var html = '<div><div class="wdjp"><a href="javascript:myhits1();"><img src="img/img_wodejiangpin@3x.png" /></a></div><div class="hdjs"><a href="activity-rule.html?id='+getParam('id')+'"><img src="img/img_huodongguize@3x.png" /></a></div></div>' ;
			$(this.ops.elem).append(html);
		},
		
		showSuccess :function(){
			var _this = this ;
			// var result = this.getMoney() + this.ops.unit;

            var html = '';

            if (prizes.length != 0){
                //拼接抽中的奖品
                // html =  '<div class="createAlertInfo" id="createAlertInfo">'+
                //     '<div class="con">'+
                //     '<img src="img/gx.png" />'+
                //     '<p>'+prizes.join('，')+'</p>'+
                //     '<a href="javascript:;" id="closeShow">关闭</a>'+
                //     '</div>'+
                //     '</div>';
				html =  '<div class="createAlertInfo" id="createAlertInfo">'+
				    '<div class="con">'+
					'<a href="javascript:;" id="closeShow"><img src="img/img_tc_yh@3x.png" /></a>'+
				    '<p style="width: 60%;margin-top: -288px;position: absolute;color:#000;left: 22%;">'+prizes.join('，')+'</p>'+
				    
				    '</div>'+
					'<div class="con2">'+
						'<a href="index.html"><img  src="img/btn_tc_home.png" /></a>'+
						'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp'+
						'<a href="my.html"><img  src="img/btn_tc_mine.png" /></a>'+
					'</div>'+
				    '</div>';
            }else{
                // 未中奖效果
                html =  '<div class="createAlertInfo" id="createAlertInfo">'+
                    '<div class="con">'+
                    '<a href="javascript:;" id="closeShow"><img src="img/img_tc_yihan@3x.png" /></a>'+
                    
                    '</div>'+
					'<div class="con1">'+
						'<a href="index.html"><img  src="img/btn_tc_home@3x.png" /></a>'+
						'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp'+
						'<a href="my.html"><img  src="img/btn_tc_mine@3x.png" /></a>'+
					'</div>'+
                    '</div>';
            }
			$(this.ops.elem).append(html);
			this.createAlertInfo = $("#createAlertInfo") ;
			setTimeout(function(){_this.createAlertInfo.find(".con").addClass("on");},10);
			$("#closeShow").on("click",function(){
				_this.hideSuccess();
			});
		},
		//活动结束，点击关闭
		hideSuccess :function(){
			var _this = this ;
			this.createAlertInfo.find(".con").removeClass("on");
			setTimeout(function(){_this.createAlertInfo.remove();},300);
			//跳到首页
			//window.location.href='index.html';
		},
		start : function(){
			var _this = this ;
			var spans = [] ;
			for( var i=this.ops.readyTime;i>=0;i-- ){
				var txt = i ;
				if( i == 0 ){txt = motion.txt;}
				spans.push('<span class="readyTimeSpan">'+txt+'</span>');
			}
			var html3 =  '<div class="nav" style="margin-top:-11.6875rem" id="readyTimeBox3">'+
						 '<div class="img-close">'+
						 '<a href="index.html">'+
						 '<img style="width:44px;height:44px;" src="./img/btn_nav_back@3x.png" onclick="javascript:location.href=index.html" />'+
						 '</a>'+
						 '</div>'+
						 '<div class="intro">'+gamename+
						 '</div>'+
						 '<div class="img-share">'+
						 '<a href="#" onclick="showList3()">'+
						 '<img style="width:44px;height:44px;" src="./img/btn_nav_share@3x.png" />'+
						 '</a>'+
						 '</div>'
						'</div>';
						
			html1 = '';
            if(motion.first){
                html1 =  '<div class="readyTimeBox1" id="readyTimeBox1"><img style="width:250px;height:107px" src="./img/img_daojishi@3x.png" />'
                '</div>'
            }
						
			var html =  '<div class="readyTimeBox" id="readyTimeBox">'+
							spans.join("")+
						'</div>';
			var html2 =  '<div class="imme_moon" id="readyTimeBox2"><div class="img_bg_moon"><img  src="./img/img_bg_moon.png" /></div><div class="huodongguize"><a href="activity-rule.html?id='+getParam('id')+'"><img  src="./img/img_huodongguize@3x.png" /></a></div>'
						 '</div>';
			$(this.ops.elem).append(html1);
			$(this.ops.elem).append(html2);
			$(this.ops.elem).append(html3);
			$(this.ops.elem).append(html);
			this.readyTimeBox1 = $("#readyTimeBox1") ;
			this.readyTimeBox2 = $("#readyTimeBox2") ;
			this.readyTimeBox3 = $("#readyTimeBox3") ;
			this.readyTimeSpans = $(".readyTimeSpan") ;
			this.readyTimeBox = $("#readyTimeBox") ;
			function showText(){
				_this.readyTimeSpans.removeClass("on").removeAttr("style");
				var current = _this.readyTimeSpans.eq(timeIndex) ;
				current.css({
					webkitTransition : '0.3s' ,
					transition:"0.3s"
				})
				setTimeout(function(){current.addClass("on");},10);
			}
			var initTime = 0 , max = this.ops.readyTime*1000 , timeIndex = 0;
			showText();
			this.readyTimer = setInterval(function(){
				initTime += 1000 ;
				timeIndex++ ;
				showText();
				if( initTime >= max ){
					clearInterval(_this.readyTimer);
					_this.readyTimer = null ;
					setTimeout(function(){
						_this.readyTimeBox.remove() ;
						_this.readyTimeBox1.remove() ;
						_this.readyTimeBox2.remove() ;
						_this.readyTimeBox3.remove() ;
						_this.play();
					},motion.time);
				}
			},motion.time);
		},
		play : function(){
			this.stop();
			var _this = this ;
			function move(){
				_this._createItems();
				_this.timer = setTimeout(move,_this.ops.intervalTime);
			}
			move();
			var currentTime = 0 ;
			var maxTime = _this.ops.time*1000 ; 
			_this._createTimeout();
			_this._createFooter();
			_this._createAward();
			_this._createNav();
			setTimeout(function(){timeoutStart();},50)
			function timeoutStart(){
				endtime--;
				if( endtime <= 0 ){
					_this.stop();
					_this.showSuccess();
					_this.ops.timeout && _this.ops.timeout();
				}else{	  
					var pro = $('.gress').attr('value');			
								var timer = null;
								 timer = setInterval(function(){
								 	pro --;
								 	if(pro ==0){
								 		clearInterval(timer);
								 	}
									$('.gress').attr({value : pro})
									$('.z1').html(pro + '秒');
								},1000);
				}
			}
			//每一秒执行一次
			this.timeoutTimer = setInterval(timeoutStart,1000);
		},
	}
	window.Bouns = Bouns ;
})();
