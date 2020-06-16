
var loader = {
	
	downFromUrl : function(url) {
		var $triggerBtn = $('<a id="downTriggerBtn" href="' + url + '" download="blo.png">blo.png</a>');
		$triggerBtn.get(0).click();
	},
	
	downFromBlob : function(blob) {
		var url = window.URL.createObjectURL(blob);
		downFromUrl(url);
	}
}