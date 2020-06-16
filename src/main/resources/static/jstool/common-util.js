/**
 * 通用工具
 */

var commonUtil = {
	// 设置多个元素隐藏或显示
	setIsHidden : function(opt) {
		$.each(opt, function(key, val) {
			if (val) {
				$(key).addClass('hidden');
			} else {
				$(key).removeClass('hidden');
			}
		});
	},
	// 设置多个元素html文本
	setElementHtml : function(opt) {
		$.each(opt, function(key, val) {
			$(key).html(val);
		});
	},
	// 设置多个元素value值
	setElementVal : function(opt) {
		$.each(opt, function(key, val) {
			$(key).val(val);
		});
	},
	// opt 验参配置，如：{rules: {"number" : {notEmpty, ""}},datas : {"number" : number}
	// checkAll 验参失败后是否继续下一个变量的验参
	// separator 多个错误返回值的分隔符（验参失败后继续下一个变量的情况下才会有用）
	valid : function(opt, checkAll, separator) {
		var rules = opt.rules;
		var datas = opt.datas;
		
		var errStr = "";
		//{rules: {"number" : {notEmpty, ""}},datas : {"number" : number}
		for(var name in rules) {
			var methodOpt = rules[name];// 获取对某个变量的验参配置
			for(var methodName in methodOpt) {
				var value = datas[name];
				
				var invokeMethod = methodName + "(value)";// 需要方法调用的编辑
				if (methodName.indexOf('.') == -1) {// 方法属于commonUtil定义的内部方法
					invokeMethod = "commonUtil." + invokeMethod;
				}
				var valid = eval(invokeMethod);// 调用验参方法
				if(!valid) {// 验参失败
					if (!checkAll) {// 验参失败不再继续验参
						return methodOpt[methodName];
					} else {
						errStr += methodOpt[methodName] + (separator ? separator : "|");// 使用分隔符分隔多个错误信息
						break;// 跳过该变量的后续验参
					}
				}
			}
		}
		if (errStr != "") {
			errStr = errStr.substr(0, errStr.length - 1);
		} else {
			errStr = null;
		}
		return errStr;
	},
	notBlank : function(val) {
		return val != null && val != undefined && val.trim() != '';
	},
	notEmpty : function(val) {
		return val != null && val != undefined && val != '';
	},
	// 一天的起始时刻
	setDateStart : function (date) {
		return new Date(date.getFullYear(), date.getMonth(), date.getDate());
	},
	afterToday : function(val) {
		var today = commonUtil.setDateStart(new Date());
		var date = commonUtil.parseDate(val);
		date = commonUtil.setDateStart(date);
		return date.getTime() > today.getTime();
	},
	notAfterToday : function(val) {
		return !commonUtil.afterToday(val);
	},
	beforeToday : function(val) {
		var today = commonUtil.setDateStart(new Date());
		var date = commonUtil.parseDate(val);
		date = commonUtil.setDateStart(date);
		return date.getTime() < today.getTime();
	},
	notBeforeToday : function(val) {
		return !commonUtil.beforeToday(val);
	},
	// 字符串转为日期，字符串必须包含yyyy-MM-dd
	parseDate : function(dateStr) {
		var splitDT = dateStr.split(" ");
		var hour = 0;
		var minute = 0;
		var second = 0;
		var splitYMD = splitDT[0].split("-");
		year = parseInt(splitYMD[0]);
		month = parseInt(splitYMD[1]) - 1;
		day = parseInt(splitYMD[2]);
		if(splitDT[1] != undefined){
			var splitHMS = splitDT[1].split(":");
			hour = parseInt(splitHMS[0]);
			minute = parseInt(splitHMS[1]);
			second = parseInt(splitHMS[2]);
			hour = isNaN(hour) ? 0 : hour;
			minute = isNaN(minute) ? 0 : minute;
			second = isNaN(second) ? 0 : second;
		}
		return new Date(year, month, day, hour, minute, second);
	},
	isUrl: function(str) {
		var exp = new RegExp(/(http(s)?:\/\/)?([\w-]+\.)+[\w-]+(\/[\w- .\/?%&=]*)?/);
		return exp.test(str);
	},
	isUrlWithProtocol: function(str) {
		var exp = new RegExp(/http(s)?:\/\/([\w-]+\.)+[\w-]+(\/[\w- .\/?%&=]*)?/);
		return exp.test(str);
	},
	// 文件大小转为可读性格式
	humanFileSize : function(num){
		var units = [" B", ' KB', " MB", " GB"];
		var unitLength = units.length;
		for(var i = 0; i < unitLength; i++){
			if(i == unitLength -1 || num < (1 << (10 * (i + 1)))){
				var c = Math.round((num * 100) / (1 << (10 * i)));
				var z = parseInt(c / 100);
				var r = c % 100;
				if (r == 0) {
					return z + units[i];
				} else if (r < 10) {
					return z + ".0" + r + units[i];
				} else {
					return z + "." + r + units[i];
				}
			}
		}
	},
	// 去除标签和引号
	escapeHtmlAndQuot: function(str) {
		if (!str) return "";
		return str.replace(new RegExp("'", "gm"), "").replace(new RegExp('"', "gm"), "")
			.replace(new RegExp("<", "gm"), "&lt;").replace(new RegExp(">", "gm"), "&gt;");
	},
	escapeLtGt: function(str) {
		if (!str) return "";
		return str.replace(new RegExp("<", "gm"), "&lt;").replace(new RegExp(">", "gm"), "&gt;");
	},
	// 移除脚本标签
	escapeScript: function(str) {
		if (!str) return "";
		return str.replace(/<script([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/script>/gm, "");
	},
	escapeHtml: function(str) {
		if (!str) return "";
		// 移除脚本标签
		str = str.replace(/<script([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/script>/gm, "");
		// 移除注释标签
		str = str.replace(/\<!--[\s\S]*?--\>/gm, "");
		// 需要移除整个标签（包含内部内容）的
		str = str.replace(/(<head([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/head>)|(<title([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/title>)|(<script([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/script>)|(<video([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/video>)|(<audio([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/audio>)|(<style([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/style>)|(<colgroup([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/colgroup>)|(<select([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/select>)|(<img([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/img>)|(<option([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/option>)|(<optgroup([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/optgroup>)|(<link([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/link>)|(<meta([\s]+[^\s>]+?)*[\s]*>[\s\S]*?<\/meta>)/gm, "");
		// 需要替换成换行符的
		str = str.replace(/(<div([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<p([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<br([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<iframe([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<html([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<table([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<thead([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<tr([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<hr([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<h1([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<h2([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<h3([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<h4([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<h5([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<li([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<tbody([\s]+[^\s>]+?)*[\s]*[\/]?>)/gm, "\n");
		// 需要替换成空格的
		str = str.replace(/(<a([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/a>)|(<td([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/td>)|(<th([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/th>)|(<blockquote([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/blockquote>)|(<form([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/form>)|(<nav([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/nav>)|(<code([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/code>)|(<body([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/body>)/gm, " ");
		// 仅有头标签或尾标签移除的
		str = str.replace(/(<strike([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/strike>)|(<span([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/span>)|(<label([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/label>)|(<ul([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/ul>)|(<ol([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/ol>)|(<u([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/u>)|(<col([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/col>)|(<b([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/b>)|(<input([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/input>)|(<button([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/button>)|(<i([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/i>)|(<head([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/head>)|(<title([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/title>)|(<script([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/script>)|(<video([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/video>)|(<audio([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/audio>)|(<style([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/style>)|(<colgroup([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/colgroup>)|(<select([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/select>)|(<img([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/img>)|(<option([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/option>)|(<optgroup([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/optgroup>)|(<link([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/link>)|(<meta([\s]+[^\s>]+?)*[\s]*[\/]?>)|(<\/meta>)|(<\/div>)|(<\/p>)|(<\/br>)|(<\/iframe>)|(<\/html>)|(<\/table>)|(<\/thead>)|(<\/tr>)|(<\/hr>)|(<\/h1>)|(<\/h2>)|(<\/h3>)|(<\/h4>)|(<\/h5>)|(<\/li>)|(<\/tbody>)/gm, "");
		// 两个换行之间的多个空白字符，包含（头尾两换行）替换成单个换行
		str = str.replace(/[\n][\s]*[\n]/gm, "\n");
		// 多个空格和制表符，替换成两个空格
		str = str.replace(/[ \t]{3,}/gm, "  ");
		return str;
	},
	// 深度合并
	deepMerge : function (obj1, obj2) {
		var stack = [];
		var obj = obj1;
		stack.push({
			o1: obj,
			o2: obj2,
		});
		while (stack.length > 0) {
			var p = stack.shift();
			var o1 = p.o1, o2 = p.o2;
			for (var key in o2) {
				if (o2[key] === undefined) {
					delete o1[key];
					continue;
				}
				if (o2[key] === null) {
					o1[key] = null;
					continue;
				}
				if (o1[key] == o2[key]) continue; // 解决掉一些循环引用的情况
				if ((o1[key] == null) || (o2[key].constructor != Object)) {
					o1[key] = o2[key];
					continue;
				}
				stack.push({
					o1: o1[key],
					o2: o2[key],
				});
			}

		}
		return obj;
	},
	// 每毫秒生成10亿个ID的情况下，有0.14%的概率发生重复
	// 实际每毫秒能生成ID的数量级为350，以此频率生成ID并运行3小时，有0.5%的概率出现重复
	// 每毫秒生成1个ID的情况下，生成重复ID的概率为0
	uuid : function () {
		var t = new Date().getTime().toString(2);
		t = "00000000" + t;
		t = t.substring(t.length - 48, t.length);
		t = "11" + t;
		var uuid = (10 + parseInt(Math.random() * 22)).toString(32);
		for (var i = 0; i < 5; i++) {
			uuid = uuid + parseInt(Math.random() * 32).toString(32);
		}
		uuid = uuid + '-';
		uuid = uuid + parseInt(Math.random() * 32).toString(32);
		uuid = uuid + parseInt(Math.random() * 32).toString(32);
		uuid = uuid + parseInt(t, 2).toString(32);
		return uuid;
	}
}

// 扩充Date类方法
// 日期转字符串格式化
Date.prototype.format = function(fmt) {
    var o = {
    	//月份 
        "M+": this.getMonth() + 1,
        //日 
        "d+": this.getDate(),
        //小时 
        "h+": this.getHours(),
        //分 
        "m+": this.getMinutes(),
        //秒 
        "s+": this.getSeconds(),
        //季度 
        "q+": Math.floor((this.getMonth() + 3) / 3),
        //毫秒 
        "S": this.getMilliseconds() 
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        }
    }
    return fmt;
}

// 扩充String类方法
// 特定字符打头，无正则匹配
String.prototype.startWith = function(str){
	if (str == null) {
		return false;
	}
	if (this.length < str.length) {
		return false;
	}
	for (var i = 0; i < str.length; i++) {
		if (this.substring(i, i + 1) !=  str.substring(i, i + 1)) {
			return false;
		}
	}
	return true;
}
// 特定字符结尾
String.prototype.endWith = function(str){
	if (str == null) {
		return false;
	}
	var len = this.length;
	var strlen = str.length;
	if (len < strlen) {
		return false;
	}
	for (var i = 0; i < strlen; i++) {
		if (this.substring(len - i - 1, len - i) !=  str.substring(strlen - i - 1, strlen - i)) {
			return false;
		}
	}
	return true;
}
// 字面意义替换，不用正则
String.prototype.literalReplace = function(mstr, str) {
	if (this.length < mstr.length) return this;
	if (this.indexOf(mstr) < 0) {
		return this;
	}
	var resStr = '';
	var rawStr = this;
	var index;
	while ((index = rawStr.indexOf(mstr)) >= 0) {
		resStr += rawStr.substring(0, index);
		resStr += str;
		rawStr = rawStr.substring(index + mstr.length);
	}
	resStr += rawStr;
	return resStr;
}
// 用于替换的字符直接原样替换进去
String.prototype.liteReplace = function(reg, str) {
	return this.replace(reg, function() {
		return str;
	});
}

String.prototype.isNumber = function() {
	var reg = /^((-?[0-9]+)|(-?[0-9]*\.[0-9]+))$/;
	return reg.test(this);
}

String.prototype.isInteger = function() {
	var reg = /^((-?[0-9]+)|(-?0[xX][0-9a-fA-F]+))$/;
	return reg.test(this);
}