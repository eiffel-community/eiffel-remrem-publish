function getLatestVersion(type,extn) {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "https://jitpack.io/v/Ericsson/eiffel-remrem-publish.svg");
    xhr.responseType = "blob";//force the HTTP response, response-type header to be blob
    xhr.onload = function() {
        var blob = xhr.response;
        var reader = new FileReader();
        reader.addEventListener("loadend", function() {
            var result = reader.result;
            if(result=="Repo not found or no access token provided"){
                document.getElementById("downloadLink").innerHTML = result;
                document.getElementById("downloadLink").style.visibility = "";
            } else {
                document.getElementById("downloadLink").innerHTML = result;
                var val = document.getElementsByTagName("text")[2].innerHTML;
                val = val.trim();
                urlLink = "https://jitpack.io/com/github/Ericsson/eiffel-remrem-publish/publish-"+type +"/"+val+"/publish-"+type +"-"+val+"."+extn;
                var link = document.createElement("a");
                link.setAttribute('id',"MYLINK");
                link.href = urlLink;
                document.body.appendChild(link);
                link.setAttribute("type", "hidden");
                link.click();
            }
        });
        reader.readAsBinaryString(blob);
    }
    xhr.send();
}