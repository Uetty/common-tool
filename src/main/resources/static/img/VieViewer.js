/**
 * 图片浏览插件
 */

var VieViewer = (function() {
	var ConflictVieView = window.VieViewer;
	function VieViewer(src, div, width, height) {
		this.constructor.nonConflict = function(newClassName) {
            window[newClassName] = window.VieViewer;
            window.VieViewer = ConflictVieView;
            this.nonConflictClass = newClassName;
		}
		
		var thisViewer = this;
		
		this.rawSrc = src;
		this.view = $(div);
		this.rate = 1.2;
		this.maxScale = 2.58;
		this.minScale = 0.5;
		this.src = null;
		
		this.transformMatrix = new TransformMatrix(1, 0, 0, 1, 0, 0);
		
		this.angle = 0;
		this.xFlip = false;
		this.yFlip = false;
		
		if (!this.view && this.view.length == 0) {
			this.view = $('<div></div>');
		}
		this.wrapper = $('<div class="view-wrapper"></div>');
		this.view.addClass('vie-viewer');
		this.view.height(height);
		this.view.height(width);
		this.view.append(this.wrapper);
		this.view.append('<div class="view-footer"><ul class="view-tool"></ul></div>');
		this.view.find('.view-tool').append('<li class="view-flip-horizontal"></li>');
		this.view.find('.view-tool').append('<li class="view-flip-vertical"></li>');
		this.view.find('.view-tool').append('<li class="view-rotate-left"></li>');
		this.view.find('.view-tool').append('<li class="view-download"></li>');
		this.view.find('.view-tool').append('<li class="view-rotate-right"></li>');
		this.view.find('.view-tool').append('<li class="view-zoom-out"></li>');
		this.view.find('.view-tool').append('<li class="view-zoom-in"></li>');
		
		function TransformMatrix(m11, m12, m21, m22, dx, dy) {
			this.m11 = m11,this.m12 = m12, this.m21 = m21, this.m22 = m22, this.dx = dx, this.dy = dy;
			this.combine = function(matrix) {
				var n11 = matrix.m11 * this.m11 + matrix.m21 * this.m12;
				var n12 = matrix.m12 * this.m11 + matrix.m22 * this.m12;
				var n21 = matrix.m11 * this.m21 + matrix.m21 * this.m22;
				var n22 = matrix.m12 * this.m21 + matrix.m22 * this.m22;
				var nx = matrix.m11 * this.dx + matrix.m21 * this.dy + matrix.dx;
				var ny = matrix.m12 * this.dx + matrix.m22 * this.dy + matrix.dy;
				return new TransformMatrix(n11, n12, n21, n22, nx, ny);
			}
		}
		function uuid(len, radix) {
			var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
			var uuid = [],i;
			radix = radix || chars.length;
			if (len) {
				// Compact form
				for (i = 0; i < len; i++) uuid[i] = chars[0 | Math.random() * radix];
			} else {
				// rfc4122, version 4 form
				var r;
				// rfc4122 requires these characters
				uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
				uuid[14] = '4';
				
				// Fill in random data. At i==19 set the high bits of clock sequence as
				// per rfc4122, sec. 4.1.5
				for (i = 0; i < 36; i++) {
					if (!uuid[i]) {
						r = 0 | Math.random() * 16;
						uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
					}
				}
			}
			return uuid.join('');
		}
		this.bindZoom = function() {
			var _this = this;
			this.view.find('.view-zoom-in').unbind('click').on('click', function() {
				_this.zoomIn();
			});
			this.view.find('.view-zoom-out').unbind('click').on('click', function() {
				_this.zoomOut();
			});
		}
		this.bindFlip = function() {
			var _this = this;
			this.view.find('.view-flip-horizontal').unbind('click').on('click', function() {
				_this.horizontalFlip();
			});
			this.view.find('.view-flip-vertical').unbind('click').on('click', function() {
				_this.verticalFlip();
			});
		}
		this.bindDownload = function() {
			var _this = this;
			this.view.find('.view-download').unbind('click').on('click', function() {
				_this.downloadRawImage();
			});
		}
		this.bindRotate = function() {
			var _this = this;
			this.view.find('.view-rotate-left').unbind('click').on('click', function(){
				_this.rotate(-90);
			});
			this.view.find('.view-rotate-right').unbind('click').on('click', function(){
				_this.rotate(90);
			});
		}
		
		// 鼠标滚动事件（兼容谷歌和火狐）
		this.onmousewheel = function(event){
			var delta = 0;
			if (event.wheelDelta != null && event.wheelDelta != undefined) {// 谷歌
				delta = event.wheelDelta / 120;
			} else {// 火狐
				delta = event.detail / -3;
			}
			if (delta < 0) {// 向下滚，缩小
				thisViewer.zoomOut();
			} else if (delta > 0) {// 向上滚，放大
				thisViewer.zoomIn();
			}
		}
		// 鼠标滚动事件（兼容谷歌和火狐）
		this.bindWheelScroll = function() {
			if (this.canvas.get(0).onmousewheel === undefined) {// 火狐
				this.canvas.get(0).removeEventListener('DOMMouseScroll', this.onmousewheel);
				this.canvas.get(0).addEventListener('DOMMouseScroll', this.onmousewheel);
			} else { // 谷歌
				this.canvas.get(0).removeEventListener('mousewheel', this.onmousewheel);
				this.canvas.get(0).addEventListener('mousewheel', this.onmousewheel);
			}
		}
		
		this.bindMoveEvent = function() {
			var _this = this;
			this.canvas.unbind('mousedown').on('mousedown', function(downEvent){
				if (downEvent.button != 0) {// 不是鼠标左键
					return true;
				}
				var _thisEl = $(this);//获取到控件
				var oriLeft = _thisEl.offset ().left;// 左侧边界位置
				var oriTop = _thisEl.offset().top;
				var oriWidth = _thisEl.width();
				var oriHeight = _thisEl.height();
				var oriRight = oriLeft + oriWidth;
				var oriBottom = oriTop + oriHeight;
				var oriMarginLeft = parseInt(_thisEl.css('marginLeft'));
				var oriMarginTop = parseInt(_thisEl.css('marginTop'));
				
				var _parentEl = _thisEl.parent();;
				var pLeft = _parentEl.offset().left;
				var pTop = _parentEl.offset().top;
				var pWidth = _parentEl.width();
				var pHeight = _parentEl.height();
				var pRight = pLeft + pWidth;
				var pBottom = pTop + pHeight;
				
				var startPosX = downEvent.pageX;// 鼠标起始位置
				var startPosY = downEvent.pageY;
				var startTime = new Date().getTime();
				// 右侧边界位置
				var $maskLaver = $('<div class="move-mask-layer"></div>');
				_parentEl.append($maskLaver);
				
				$maskLaver.on('mousemove', function (moveEvent) {
					var px = moveEvent.pageX;
					var py = moveEvent.pageY;
					//获取移动的宽度
					var moveX = moveEvent.pageX - startPosX;
					var moveY = moveEvent.pageY - startPosY;
					
					if (moveX + oriLeft > pLeft && moveX + oriRight > pRight) {
						if (oriLeft <= pLeft) {
							moveX = pLeft - oriLeft;
						} else {
							moveX = pRight - oriRight;
						}
					}
					
					if (moveX + oriRight < pRight && moveX + oriLeft < pLeft) {
						if (oriRight >= pRight) {
							moveX = pRight - oriRight;
						} else {
							moveX = pLeft - oriLeft;
						}
					}
					if (moveY + oriTop > pTop && moveY + oriBottom > pBottom) {
						if (oriTop <= pTop) {
							moveY = pTop - oriTop;
						} else {
							moveY = pBottom - oriBottom;
						}
					}
					if (moveY + oriBottom < pBottom && moveY + oriTop < pTop) {
						if (oriBottom >= pBottom) {
							moveY = pBottom - oriBottom;
						} else {
							moveY = pTop - oriTop;
						}
					}
					var marginLeft = oriMarginLeft + moveX;
					var marginTop = oriMarginTop + moveY;
					
					_thisEl.css('marginLeft', marginLeft + 'px');
					_thisEl.css('marginTop', marginTop + 'px');
					
					//最后返回false;
					return false;
				});
				//鼠标松开清空所有事件
				$maskLaver.on('mouseup', function (upEvent) {
					$maskLaver.unbind('mousemove');
					$maskLaver.unbind('mouseup');
					window.onmousemove = null;
					window.onmouseup = null;
					
					var layer = _parentEl.find('.move-mask-layer');
					_parentEl.find('.move-mask-layer').remove();
					
				});
				return false;
			});
		}
		
		this.loadCanvas = function() {
			var $childs = this.wrapper.children('canvas');
			if ($childs.length > 0) {
				this.canvas = $childs.eq(0);
			}
			var $canvas = $('<canvas>当前浏览器不支持canvas标签</canvas>');
			if (this.canvas) {
				$canvas.attr('class', this.canvas.attr('class'));
				$canvas.attr('style', this.canvas.attr('style'));
				$canvas.attr('id', this.canvas.attr('id'));
				this.canvas.remove();
				this.canvas = null;
			}
			this.wrapper.append($canvas);
			this.canvas = $canvas;
		}
		this.setDefaultScale = function() {
			var imageLength = this.img.width;
			
			var wrapHeight = this.wrapper.height();
			var wrapWidth = this.wrapper.width();
			
			this.scale = Math.min(wrapHeight, wrapWidth) / imageLength;;
			
			var maxlimit = this.maxScale;
			var minlimit = this.minScale;
			
			if (maxlimit < this.scale) {
				maxlimit = this.scale;
			}
			if (minlimit > this.scale) {
				minlimit = this.scale;
			}
			
			if (minlimit != this.minScale && maxlimit != this.maxScale
					|| minlimit == this.minScale && maxlimit == this.maxScale) {
				this.maxScale = this.scale * Math.ceil(maxlimit / this.scale);
				this.minScale = this.scale / Math.ceil(this.scale / minlimit);
			} else if (minlimit != this.minScale) {
				this.minScale = this.scale / Math.ceil(this.scale / minlimit);
				this.maxScale = this.minScale * Math.pow(this.rate, 8); 
			} else {
				this.maxScale = this.scale * Math.ceil(maxlimit / this.scale);
				this.minScale = this.maxScale / Math.pow(this.rate, 8);
			}
		}
		this.getSquareImageLength = function(width, height) {
//		return Math.ceil(Math.sqrt(width * width + height * height));
			return Math.max(width, height);
		}
		this.loadSquareImage = function(onsuccess, img=null) {
			var viewer = this;
			if (!img) {
				img = new Image();
			}
			img.setAttribute('crossOrigin', 'anonymous');
			img.onload = function() {
				var imgWidth = this.width;
				var imgHeight = this.height;
				
				viewer.hwRatio = imgHeight / imgWidth;// 记录宽高比，预留后面可能需要使用
				var $canvas = $('<canvas></canvas>');
				
				var length = viewer.getSquareImageLength(imgWidth, imgHeight);
				$canvas.get(0).height = length;
				$canvas.get(0).width = length;
				var context = $canvas.get(0).getContext('2d');
				
				var x = (length - imgWidth) / 2, y = (length - imgHeight) / 2;
				context.drawImage(this, 0, 0, imgWidth, imgHeight, x, y, imgWidth, imgHeight);
				$canvas.get(0).toBlob(function(blob) {
					var url = window.URL.createObjectURL(blob);
					onsuccess(url);
				});
			}
			img.onerror = viewer.onerror; 
			img.src = viewer.rawSrc;
		}
		
		this.addRotate = function(angle, centerX, centery) {
			var rotateMatrix = this.getRotateMatrix(angle, centerX, centery);
			this.transformMatrix = this.transformMatrix.combine(rotateMatrix);
		}
		
		this.getRotateMatrix = function(angle, centerX, centerY) {
			var sin = Math.sin(Math.PI * angle / 180);
			var cos = Math.cos(Math.PI * angle / 180);
			var matrix = new TransformMatrix(cos, sin, -sin, cos,
					-centerX * cos + centerY * sin + centerX, -centerX * sin - centerY * cos + centerY);
			return matrix;
		}
		
		this.addTurn = function(xFlip, yFlip, centerX, centerY) {
			var turnMatrix = this.getTurnMatrix(xFlip, yFlip, centerX, centerY);
			this.transformMatrix = this.transformMatrix.combine(turnMatrix);
		}
		this.getTurnMatrix = function(xFlip, yFlip, centerX, centerY) {
			var m11 = xFlip ? -1 : 1;
			var m22 = yFlip ? -1 : 1;
			var dx = xFlip ? 2 * centerX : 0;
			var dy = yFlip ? 2 * centerY : 0;
			var m12 = 0, m21 = 0;
			return new TransformMatrix(m11, m12, m21, m22, dx, dy);
		}
		
		this.addZoom = function(scale, centerX, centerY) {
			var zoomMatrix = this.getZoomMatrix(scale, centerX, centerY);
			this.transformMatrix = this.transformMatrix.combine(zoomMatrix);
		}
		this.getZoomMatrix = function(scale, centerX, centerY) {
			var m11 = scale;
			var m22 = scale;
			var m12 = 0, m21 = 0, dx = 0, dy = 0;
			return new TransformMatrix(m11, m12, m21, m22, dx, dy);
		}
		
		this.drawImage = function() {
			// 旋转时计算新坐标轴下旋转后宽高
			var width = this.img.width;
			var height = this.img.height;
			
			if (!this.scale) {
				this.setDefaultScale();
			}
			var h = width * this.scale;
			var w = height * this.scale;
			this.canvas.get(0).height = h;
			this.canvas.get(0).width = w;
			
			var context = this.canvas.get(0).getContext('2d');
			context.clearRect(0, 0, width, height);
			context.save();
			this.transformMatrix = new TransformMatrix(1, 0, 0, 1, 0, 0);
			
			this.angle = this.angle % 360;
			this.addRotate(this.angle, width / 2, height / 2);
			
			this.addTurn(this.xFlip, this.yFlip, width / 2, height / 2);
			
			this.addZoom(this.scale, width / 2, height / 2);
			
			var m11 = this.transformMatrix.m11;
			var m12 = this.transformMatrix.m12;
			var m21 = this.transformMatrix.m21;
			var m22 = this.transformMatrix.m22;
			var dx = this.transformMatrix.dx;
			var dy = this.transformMatrix.dy;
			context.transform(m11, m12, m21, m22, dx, dy);
			
			context.drawImage(this.img, 0, 0);
			context.restore();
		}
		
		this.onerror = function(e) {
			console.log('error');
		}
		
		this.load = function(callback, img) {
			var viewer = this;
			
			// actual action
			var action = function(src) {
				if (src) {
					viewer.src = src;
				}
				viewer.img = new Image();
				viewer.img.setAttribute('crossOrigin', 'anonymous');
				viewer.img.onload = function() {
					viewer.drawImage();
					viewer.adjustMargin();
					viewer.bindMoveEvent();
					viewer.bindWheelScroll();
					viewer.bindRotate();
					viewer.bindDownload();
					viewer.bindZoom();
					viewer.bindFlip();
					if (callback) {
						callback();
					}
				}
				viewer.img.src = viewer.src;
			}
			
			if (!this.src) {
				this.loadSquareImage(action, img);
			} else {
				action();
			}
		}
		this.adjustMargin = function() {
			
			var _thisEl = this.canvas;//获取到控件
			var _parentEl = _thisEl.parent();;
			
			var oriMarginLeft = parseInt(_thisEl.css('marginLeft'));
			var oriMarginTop = parseInt(_thisEl.css('marginTop'));
			
			var oriLeft = _thisEl.offset ().left;// 左侧边界位置
			var oriTop = _thisEl.offset().top;
			var oriWidth = _thisEl.width();
			var oriHeight = _thisEl.height();
			var oriRight = oriLeft + oriWidth;
			var oriBottom = oriTop + oriHeight;
			var pLeft = _parentEl.offset().left;
			var pTop = _parentEl.offset().top;
			var pWidth = _parentEl.width();
			var pHeight = _parentEl.height();
			var pRight = pLeft + pWidth;
			var pBottom = pTop + pHeight;
			
			var midX = (oriLeft + oriRight) / 2;
			var midY = (oriTop + oriBottom) / 2;
			var pMidX = (pLeft + pRight) / 2;
			var pMidY = (pTop + pBottom) / 2;
			
			var moveX = pMidX - midX;
			var moveY = pMidY - midY;
			var xLevel = Math.abs(moveX / pWidth);
			if (xLevel > 0.40) {
				moveX = moveX * 0.5;
			} else if (xLevel > 0.24) {
				moveX = moveX * 0.75;
			} else if (xLevel > 0.8) {
				moveX = moveX * 0.9;
			}
			var yLevel = Math.abs(moveY / pHeight);
			if (yLevel > 0.40) {
				moveY = moveY * 0.5;
			} else if (yLevel > 0.24) {
				moveY = moveY * 0.75;
			} else if (yLevel > 0.8) {
				moveY = moveY * 0.9;
			}
			
			if (moveX + oriLeft > pLeft && moveX + oriRight > pRight) {
				if (oriLeft <= pLeft) {
					moveX = pLeft - oriLeft;
				} else {
					moveX = pRight - oriRight;
				}
			}
			
			if (moveX + oriRight < pRight && moveX + oriLeft < pLeft) {
				if (oriRight >= pRight) {
					moveX = pRight - oriRight;
				} else {
					moveX = pLeft - oriLeft;
				}
			}
			if (moveY + oriTop > pTop && moveY + oriBottom > pBottom) {
				if (oriTop <= pTop) {
					moveY = pTop - oriTop;
				} else {
					moveY = pBottom - oriBottom;
				}
			}
			if (moveY + oriBottom < pBottom && moveY + oriTop < pTop) {
				if (oriBottom >= pBottom) {
					moveY = pBottom - oriBottom;
				} else {
					moveY = pTop - oriTop;
				}
			}
			var marginLeft = oriMarginLeft + moveX;
			var marginTop = oriMarginTop + moveY;
			
			_thisEl.css('marginLeft', marginLeft + 'px');
			_thisEl.css('marginTop', marginTop + 'px');
		}
		this.reload = function() {
			this.drawImage();
			this.adjustMargin();
		}
		this.toBlob = function(func) {
			this.canvas.get(0).toBlob(func);
		},
		// 旋转（顺时针为正）
		this.rotate = function(rat) {
			this.angle += rat;
			this.reload();
		}
		// 水平翻转
		this.horizontalFlip = function() {
			this.xFlip = !this.xFlip;
			this.reload();
		}
		// 垂直翻转
		this.verticalFlip = function() {
			this.yFlip = !this.yFlip;
			this.reload();
		}
		// 放大
		this.zoomIn = function() {
			if (this.scale * this.rate > this.maxScale 
					|| this.scale * this.rate < this.minScale) {
				return false;
			}
			this.scale = this.scale * this.rate;
			this.reload();
			return true;
		}
		// 缩小
		this.zoomOut = function() {
			if (this.scale / this.rate > this.maxScale 
					|| this.scale / this.rate < this.minScale) {
				return false;
			}
			this.scale = this.scale / this.rate;
			this.reload();
			return true;
		}
		
		this.downloadRawImage = function(name='') {
			var url = this.rawSrc;
			if (!name) {
				var extName;
				if ((extName = url.substring(url.lastIndexOf('.')+1))
						&& (extName == 'png' || extName == 'jpg' || extName == 'gif')) {
					name = uuid(8) + '.' + extName;
				}
			}
			var $triggerBtn = $('<a id="vie-download-btn" style="display:none;" href="' + url + '" download="' + name + '">' + name + '</a>');
			$('#vie-download-btn').remove();
			$('body').append($triggerBtn);
			document.getElementById('vie-download-btn').click();
			$('#vie-download-btn').remove();
		}
		this.downloadImage = function(name='') {
			this.toBlob(function(blob){
				var url = window.URL.createObjectURL(blob);
				if (!name) {
					var extName;
					if ((extName = url.substring(url.lastIndexOf('.')+1))
							&& (extName == 'png' || extName == 'jpg' || extName == 'gif')) {
						name = uuid(8) + '.' + extName;
					}
				}
				var $triggerBtn = $('<a href="' + url + '" download="' + name + '">' + name + '</a>');
				$triggerBtn.get(0).click();
			});
		}
		// 更换图片
		this.showNextImage = function(src) {
			this.xFlip = false;
			this.yFlip = false;
			this.scale = null;
			this.angle = 0;
			var context = this.canvas.get(0).getContext('2d');
			context.clearRect(0, 0, width, height);
			
			if (src instanceof Image) {
				var img = src;
				this.rawSrc = img.src;
				this.src = null;
				this.load(null, img);
			} else {
				this.src = null;
				this.rawSrc = src;
				this.load();
			}
		}
		this.loadCanvas();
	}
	return VieViewer;
})();