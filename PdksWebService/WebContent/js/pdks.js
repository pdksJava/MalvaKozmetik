function showDivContent(divname) {
	document.getElementById(divname).style.display = "block";
}

function hideDivContent(divname) {
	document.getElementById(divname).style.display = "none";
}

function jumpScroll(divname) {
	window.location.hash = divname;
}

function limitText(limitField, limitNum, aciklama) {
	if (limitField.value.length > limitNum) {

		limitField.value = limitField.value.substring(0, limitNum);
	}
	if (document.getElementById(aciklama))
		document.getElementById(aciklama).innerHTML = (limitNum - limitField.value.length);
}
function allowJustNumeric($char, $mozChar) {
	if ($mozChar != null) { // Look for a Mozilla-compatible browser
		if (($mozChar >= 48 && $mozChar <= 57) || $mozChar == 0
				|| $mozChar == 17 || $char == 8)
			$RetVal = true;
		else {
			$RetVal = false;
		}
	} else { // Must be an IE-compatible Browser
		if (($char >= 48 && $char <= 57))
			$RetVal = true;
		else {
			$RetVal = false;
		}
	}
	return $RetVal;
}

function allowJustNumericWithPoint($char, $mozChar) {
	if ($mozChar != null) { // Look for a Mozilla-compatible browser
		if (($mozChar >= 48 && $mozChar <= 57) || $mozChar == 0 || $char == 8
				|| $mozChar == 13 || $mozChar == 44)
			$RetVal = true;
		else {
			$RetVal = false;
		}
	} else { // Must be an IE-compatible Browser
		if (($char >= 48 && $char <= 57) || $char == 13 || $char == 44)
			$RetVal = true;
		else {
			$RetVal = false;
		}
	}
	return $RetVal;
}

function allowJustNumericWithComma(num, $char, $mozChar) {
	if ($mozChar != null) { // Look for a Mozilla-compatible browser
		if (($mozChar >= 48 && $mozChar <= 57) || $mozChar == 0 || $char == 8
				|| $mozChar == 13 || $mozChar == 44 || $mozChar == 8)
			$RetVal = true;
		else {
			$RetVal = false;
		}
	} else { // Must be an IE-compatible Browser
		if (($char >= 48 && $char <= 57) || $char == 13 || $char == 44
				|| $char == 8)
			$RetVal = true;
		else {
			$RetVal = false;
		}
	}
	return $RetVal;
}

function disableBackSapace($char, $mozChar) {
	if ($mozChar != null) { // Look for a Mozilla-compatible browser
		if ($mozChar == 8) {
			$RetVal = false;
		} else {
			$RetVal = true;
		}
	} else { // Must be an IE-compatible Browser
		if ($char == 8)
			$RetVal = false;
		else {
			$RetVal = true;
		}
	}
	return $RetVal;
}
function helpContent(divname, evnt, textm) {
	showToolTip(evnt, textm);

}
function dataTableFormat(divname, pageSize) {
	
	$(document).ready(function() {
		$('#'+divname ).DataTable( {
			"pageLength": pageSize,
			"scrollY": "95%",
			"scrollX": true,
			"scrollCollapse": true,
			"fixedColumns": true,
			"language": {
			"lengthMenu": "Sayfadaki satır _MENU_",
			"zeroRecords": "Kayıt bulunamadı",
			"info": "Sayfa _PAGE_ / _PAGES_ Toplam Kayıt Sayısı : _MAX_",
			"infoEmpty": "Kayıt bulunamadı",
			"infoFiltered": "(Filtnrelene Kayıt Sayısı : _MAX_)",
			"search": "Arama : ",
			"paginate": {
			"previous": "Önceki Sayfa",
			"next": "Sonraki Sayfa"
			}
		}
		} );
	} );

}

function showToolTip2(e, text) {
	if (document.all)
		e = event;

	var obj = document.getElementById('helpiconDiv');
	var obj2 = document.getElementById('helpiconDivContentDiv');
	obj2.innerHTML = text;
	obj.style.display = 'block';
	var st = Math.max(document.body.scrollTop,
			document.documentElement.scrollTop);
	if (navigator.userAgent.toLowerCase().indexOf('safari') >= 0)
		st = 0;
	var leftPos = e.clientX + 10;
	if (leftPos < 0)
		leftPos = 0;
	obj.style.left = (leftPos) + 'px';
	obj.style.top = (e.clientY - 30) + st + 'px';
}

function helpContent2(divname, evnt, textm) {
	showToolTip2(evnt, textm);

}
function helpContent3(divname, evnt, elem) {
	var obj = elem.id.substring(0, elem.id.lastIndexOf(':') + 1) + 'errMsg';
	textMessage = document.getElementById(obj).innerHTML;
	showToolTip3(evnt, textMessage);
}

function showToolTip3(e, text) {
	if (document.all)
		e = event;

	var obj = document.getElementById('errorToolTipDiv');
	var obj2 = document.getElementById('errorToolTipContentDiv');
	obj2.innerHTML = text;
	obj.style.display = 'block';
	var st = Math.max(document.body.scrollTop,
			document.documentElement.scrollTop);
	if (navigator.userAgent.toLowerCase().indexOf('safari') >= 0)
		st = 0;
	var leftPos = e.clientX + 10;
	if (leftPos < 0)
		leftPos = 0;
	obj.style.left = (leftPos) + 'px';
	obj.style.top = (e.clientY - 30) + st + 'px';
}

function showToolTip(e, text) {
	if (document.all)
		e = event;

	var obj = document.getElementById('helpiconDiv');
	var obj2 = document.getElementById('helpiconDivContentDiv');
	obj2.innerHTML = text;
	obj.style.display = 'block';
	var st = Math.max(document.body.scrollTop,	document.documentElement.scrollTop);
	if (navigator.userAgent.toLowerCase().indexOf('safari') >= 0)
		st = 0;
	var leftPos = e.clientX + 14;
	if (leftPos < 0)
		leftPos = 0;
	obj.style.left = (leftPos) + 'px';
	obj.style.top = (e.clientY - 100) + st + 'px';
}

function helpContentClose(divname) {
	document.getElementById('helpiconDivContentDiv').innerHTML = '';
	document.getElementById('helpiconDiv').style.display = 'none';
}
function helpContentClose2(divname) {
	document.getElementById('errorToolTipContentDiv').innerHTML = '';
	document.getElementById('errorToolTipDiv').style.display = 'none';
}

function drawVisualization() {
	// Create and populate the data table.
	var data = new google.visualization.DataTable();
	data.addColumn('string', 'Name');
	data.addColumn('string', 'Manager');
	data.addColumn('string', 'ToolTip');

	var name_array = document.getElementById('denemeForm:hiddenArray').value;
	array_temp1 = name_array.split("|").toString();
	array_temp = array_temp1.split("?");

	var size = array_temp.length;
	var i = 0;
	var j = 0;
	var x = 1;
	data.addRows(size);

	for (i = 0; i < size; i++) {

		if (i > 1) {

			data.setCell(i, i - x, i > 3 ? array_temp[i - 3]
					: array_temp[i - 1]);
			data.setCell(i, 0, array_temp[i]);
			data.setCell(i, 2, array_temp[i]);
			x++;
			j++;
		} else if (i == 1) {
			data.setCell(i, 0, array_temp[i]);
			data.setCell(i, 1, array_temp[i - 1]);
			data.setCell(i, 2, array_temp[i]);
		} else {
			data.setCell(i, i, array_temp[i]);
			data.setCell(i, 2, array_temp[i]);
		}

	}
	// Create and draw the visualization.
	new google.visualization.OrgChart(document.getElementById('visualization'))
			.draw(data, {
				allowHtml : true
			});

	google.setOnLoadCallback(drawVisualization);
}
function browserVersion() {
	
	var geckobrowsers;
	var browser = "";
	var browserVersion = 0;
	var agent = navigator.userAgent + " ";
	if(agent.substring(agent.indexOf("Mozilla/")+8, agent.indexOf(" ")) == "5.0" && agent.indexOf("like Gecko") != -1){
	    geckobrowsers = agent.substring(agent.indexOf("like Gecko")+10).substring(agent.substring(agent.indexOf("like Gecko")+10).indexOf(") ")+2).replace("LG Browser", "LGBrowser").replace("360SE", "360SE/");
	    var i;
	    for(i = 0; i < 1; i++){
	        geckobrowsers = geckobrowsers.replace(geckobrowsers.substring(geckobrowsers.indexOf("("), geckobrowsers.indexOf(")")+1), "");
	    }
	    geckobrowsers = geckobrowsers.split(" ");
	    for(i = 0; i < geckobrowsers.length; i++){
	        if(geckobrowsers[i].indexOf("/") == -1)geckobrowsers[i] = "Chrome";
	        if(geckobrowsers[i].indexOf("/") != -1)geckobrowsers[i] = geckobrowsers[i].substring(0, geckobrowsers[i].indexOf("/"));
	    }
	    if(geckobrowsers.length < 4){
	        browser = geckobrowsers[0];
	    } else {
	        for(i = 0; i < geckobrowsers.length; i++){
	            if(geckobrowsers[i].indexOf("Chrome") == -1 && geckobrowsers[i].indexOf("Safari") == -1 && geckobrowsers[i].indexOf("Mobile") == -1 && geckobrowsers[i].indexOf("Version") == -1)browser = geckobrowsers[i];
	        }
	    }
	    browserVersion = agent.substring(agent.indexOf(browser)+browser.length+1, agent.indexOf(browser)+browser.length+1+agent.substring(agent.indexOf(browser)+browser.length+1).indexOf(" "));
	} else if(agent.substring(agent.indexOf("Mozilla/")+8, agent.indexOf(" ")) == "5.0" && agent.indexOf("Gecko/") != -1){
	    browser = agent.substring(agent.substring(agent.indexOf("Gecko/")+6).indexOf(" ") + agent.indexOf("Gecko/")+6).substring(0, agent.substring(agent.substring(agent.indexOf("Gecko/")+6).indexOf(" ") + agent.indexOf("Gecko/")+6).indexOf("/"));
	    browserVersion = agent.substring(agent.indexOf(browser)+browser.length+1, agent.indexOf(browser)+browser.length+1+agent.substring(agent.indexOf(browser)+browser.length+1).indexOf(" "));
	} else if(agent.substring(agent.indexOf("Mozilla/")+8, agent.indexOf(" ")) == "5.0" && agent.indexOf("Clecko/") != -1){
	    browser = agent.substring(agent.substring(agent.indexOf("Clecko/")+7).indexOf(" ") + agent.indexOf("Clecko/")+7).substring(0, agent.substring(agent.substring(agent.indexOf("Clecko/")+7).indexOf(" ") + agent.indexOf("Clecko/")+7).indexOf("/"));
	    browserVersion = agent.substring(agent.indexOf(browser)+browser.length+1, agent.indexOf(browser)+browser.length+1+agent.substring(agent.indexOf(browser)+browser.length+1).indexOf(" "));
	} else if(agent.substring(agent.indexOf("Mozilla/")+8, agent.indexOf(" ")) == "5.0"){
	    browser = agent.substring(agent.indexOf("(")+1, agent.indexOf(";"));
	    browserVersion = agent.substring(agent.indexOf(browser)+browser.length+1, agent.indexOf(browser)+browser.length+1+agent.substring(agent.indexOf(browser)+browser.length+1).indexOf(" "));
	} else if(agent.substring(agent.indexOf("Mozilla/")+8, agent.indexOf(" ")) == "4.0" && agent.indexOf(")")+1 == agent.length-1){
	    browser = agent.substring(agent.indexOf("(")+1, agent.indexOf(")")).split("; ")[agent.substring(agent.indexOf("(")+1, agent.indexOf(")")).split("; ").length-1];
	} else if(agent.substring(agent.indexOf("Mozilla/")+8, agent.indexOf(" ")) == "4.0" && agent.indexOf(")")+1 != agent.length-1){
	    if(agent.substring(agent.indexOf(") ")+2).indexOf("/") != -1)browser = agent.substring(agent.indexOf(") ")+2, agent.indexOf(") ")+2+agent.substring(agent.indexOf(") ")+2).indexOf("/"));
	    if(agent.substring(agent.indexOf(") ")+2).indexOf("/") == -1)browser = agent.substring(agent.indexOf(") ")+2, agent.indexOf(") ")+2+agent.substring(agent.indexOf(") ")+2).indexOf(" "));
	    browserVersion = agent.substring(agent.indexOf(browser)+browser.length+1, agent.indexOf(browser)+browser.length+1+agent.substring(agent.indexOf(browser)+browser.length+1).indexOf(" "));
	} else if(agent.substring(0, 6) == "Opera/"){
	    browser = "Opera";
	    browserVersion = agent.substring(agent.indexOf(browser)+browser.length+1, agent.indexOf(browser)+browser.length+1+agent.substring(agent.indexOf(browser)+browser.length+1).indexOf(" "));
	    if(agent.substring(agent.indexOf("(")+1).indexOf(";") != -1)os = agent.substring(agent.indexOf("(")+1, agent.indexOf("(")+1+agent.substring(agent.indexOf("(")+1).indexOf(";"));
	    if(agent.substring(agent.indexOf("(")+1).indexOf(";") == -1)os = agent.substring(agent.indexOf("(")+1, agent.indexOf("(")+1+agent.substring(agent.indexOf("(")+1).indexOf(")"));
	} else if(agent.substring(0, agent.indexOf("/")) != "Mozilla" && agent.substring(0, agent.indexOf("/")) != "Opera"){
	    browser = agent.substring(0, agent.indexOf("/"));
	    browserVersion = agent.substring(agent.indexOf(browser)+browser.length+1, agent.indexOf(browser)+browser.length+1+agent.substring(agent.indexOf(browser)+browser.length+1).indexOf(" "));
	} else {
	    browser = agent;
	}
	var bn= browser + " v" + browserVersion;
	return bn; 
	
}

function tableScroll(divId, height) {
	var elements = document.getElementById(divId);
 	if (elements ) {
 		var agent = navigator.userAgent.toLowerCase() + " ";
   		if (agent.indexOf("msie")<0) {
 			if (height && height > 0)
 				elements.style.height = height + "px";
 			else
 				elements.style.height =   "450px";
			elements.addEventListener("scroll", function() {
				var translate = "translate(0," + this.scrollTop + "px)";
				this.querySelector("thead").style.transform = translate;
			});			
		}  
 	} 

}
function drawVisualization2() {

	// Create and populate the data table.
	var data = new google.visualization.DataTable();
	data.addColumn('string', 'Name');
	data.addColumn('string', 'Manager');
	data.addColumn('string', 'ToolTip');

	var name_array = document.getElementById('denemeForm:hiddenArray1').value;
	array_temp1 = name_array.split("|").toString;
	array_temp = array_temp1.split("?");

	var size = array_temp.length;
	var i = 0;
	var j = 0;
	var x = 1;
	data.addRows(size);

	for (i = 0; i < size; i++) {

		if (i > 1) {

			data.setCell(i, i - x, i > 1 ? array_temp[i - 2]
					: array_temp[i - 1]);
			data.setCell(i, 0, array_temp[i]);
			data.setCell(i, 2, array_temp[i]);
			x++;
			j++;
		} else if (i == 1) {
			data.setCell(i, 0, array_temp[i]);
			data.setCell(i, 1, array_temp[i - 1]);
			data.setCell(i, 2, array_temp[i]);
		} else {
			data.setCell(i, i, array_temp[i]);
			data.setCell(i, 2, array_temp[i]);
		}

	}
	// Create and draw the visualization.
	new google.visualization.OrgChart(document.getElementById('visualization1'))
			.draw(data, {
				allowHtml : true
			});

	google.setOnLoadCallback(drawVisualization2);
}


function tablePage(tableId,pageSize ) {
	
	$(document).ready(function() {
	    $('#'+tableId).DataTable( {
  	 
	        "pageLength": pageSize,
	        "scrollY":        "95%",
	        "scrollX":        true,
	        "scrollCollapse": true,
	        "fixedColumns":   true,
	        "language": {
	            "lengthMenu": "Sayfadaki satır _MENU_",
	            "zeroRecords": "Kayıt bulunamadı",
	            "info": "Sayfa _PAGE_ / _PAGES_ Toplam kayıt sayısı : _MAX_",
	            "infoEmpty": "Boş",
	            "infoFiltered": "(Filtrelenen kayıt sayısı : _MAX_)",
	            "search": "Arama : ",
	            "paginate": {
	                "previous": "Önceki Sayfa",
	                "next": "Sonraki sayfa"
	              }
	        }
	    } );
	} );
	

}