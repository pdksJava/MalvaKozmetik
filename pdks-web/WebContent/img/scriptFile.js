function updatePanelPassword(form) {
	
	var formName= form.id;
    var checkbox = document.getElementById(formName+":ldapuse");
    var div = document.getElementById(formName+":userEdit:optionsPanel");
    div.style.visibility = checkbox.checked ? "hidden" : "visible";
}