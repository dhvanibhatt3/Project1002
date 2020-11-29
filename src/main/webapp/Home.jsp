<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>

<%
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>


<html>
    <head>
    <link href="//maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" rel="stylesheet" id="bootstrap-css">
<script src="//maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"></script>
<script src="//code.jquery.com/jquery-1.11.1.min.js"></script>
        <title>Upload Test</title>
        <script src="https://code.jquery.com/jquery-1.9.1.min.js"></script>
        <style>
        .files input {
    outline: 2px dashed #92b0b3;
    outline-offset: -10px;
    -webkit-transition: outline-offset .15s ease-in-out, background-color .15s linear;
    transition: outline-offset .15s ease-in-out, background-color .15s linear;
    padding: 120px 0px 85px 35%;
    text-align: center !important;
    margin: 0;
    width: 100% !important;
}
.files input:focus{     outline: 2px dashed #92b0b3;  outline-offset: -10px;
    -webkit-transition: outline-offset .15s ease-in-out, background-color .15s linear;
    transition: outline-offset .15s ease-in-out, background-color .15s linear; border:1px solid #92b0b3;
 }
.files{ position:relative}
.files:after {  pointer-events: none;
    position: absolute;
    top: 60px;
    left: 0;
    width: 50px;
    right: 0;
    height: 56px;
    content: "";
    background-image: url(https://image.flaticon.com/icons/png/128/109/109612.png);
    display: block;
    margin: 0 auto;
    background-size: 100%;
    background-repeat: no-repeat;
}
.color input{ background-color:#f1f1f1;}
.files:before {
    position: absolute;
    bottom: 10px;
    left: 0;  pointer-events: none;
    width: 100%;
    right: 0;
    height: 57px;
    content: " or drag it here. ";
    display: block;
    margin: 0 auto;
    color: #2ea591;
    font-weight: 600;
    text-transform: capitalize;
    text-align: center;
}
        </style>
        <style>
            #post{
                display:none;
            }
        </style>
        <meta charset="UTF-8">
    </head>
    <body>
    <h1 id="welcome">

    </h1>
    <form action="/CloudVision" id="imageform" method="post" enctype="multipart/form-data">
    </form>
    <div id="imageDiv" class="thumbnail">
        <script>
            function imgOnclick(src) {
                var hiddenInput = '<input type="hidden" name="hiddenField" value="' + src +'"/>';
                document.getElementById('imageform').innerHTML += hiddenInput;
                document.getElementById('imageform').submit();
            }
        </script>
    </div>
    <div id="fb-root"></div>
    <script async defer crossorigin="anonymous" src="https://connect.facebook.net/en_US/sdk.js#xfbml=1&version=v8.0&appId=3404674302977168&autoLogAppEvents=1" nonce="iGO5Dyev"></script>
    <script>
        // This is called with the results from from FB.getLoginStatus().
        var accessToken;
        function statusChangeCallback(response)
        {
            console.log('statusChangeCallback');
            console.log(response);

            if (response.status === 'connected'){
                // Logged into your app and Facebook
                //document.getElementById('post').style.display="block";
                testAPI();
                pullimages();
            }

            else {
                // The person is not logged into your app or we are unable to tell.
                document.getElementById('status').innerHTML = 'Please log ' +
                    'into this app.';
                document.getElementById('post').style.display="none";
            }
        }

        // This function is called when someone finishes with the Login
        // Button.  See the onlogin handler attached to it in the sample
        // code below.
        function checkLoginState()
        {
            FB.getLoginStatus(function(response) {
                statusChangeCallback(response);
            });
        }

        window.fbAsyncInit = function() {
            FB.init({
                appId      : '2243976699080326',
                xfbml      : true,
                version    : 'v8.0'
            });

            FB.getLoginStatus(function(response) {
                statusChangeCallback(response);
            });

        };

        (function(d, s, id){
            var js, fjs = d.getElementsByTagName(s)[0];
            if (d.getElementById(id)) {return;}
            js = d.createElement(s); js.id = id;
            js.src = "https://connect.facebook.net/en_US/sdk.js";
            fjs.parentNode.insertBefore(js, fjs);
        }(document, 'script', 'facebook-jssdk'));



        // Here we run a very simple test of the Graph API after login is
        // successful.  See statusChangeCallback() for when this call is made.
        function testAPI()
        {
            console.log('Welcome!  Fetching your information.... ');
            FB.api('/me', function (response) {
                document.getElementById('welcome').innerHTML = 'Thanks for logging in, ' + response.name + '! ';
                document.getElementById('imageform').innerHTML = '<input type="hidden" name="username" value="' + response.name +'"/>';
            });

        }

        /*function pullimages() {
          FB.api('/me/album','get',{fields: 'photos{link}'} , function(response) {
            console.log(response.data);

            /*var imagelink = response.getElementById("link").value;
            response.getElementById('imagelink').innerHTML = 'The image URL is' + objectid(imagelink);
          }, {scope: 'user_birthday, user_photos, user_gender, email, public_profile'});
        }
        */



        function pullimages() {
            //const index = fbAlbumsPhotosObj.data.findIndex(album => album.id == albumId); //Get index of album

            FB.api(
                '/me/albums',
                'GET',
                {"fields":"photos{images}"},
                function(response) {
                    console.log(response);
                    var imgHtml = "";
                    response.data.forEach(album => {album.photos.data.forEach(photo => {
                        console.log(photo.id);
                        console.log(photo.images[photo.images.length - 1].source);
                        imgHtml += ' <img name="upload" value="' + photo.id + '" src="' + photo.images[photo.images.length - 1].source + '" onclick=imgOnclick("' + photo.images[photo.images.length - 1].source + '") /> ';
                    })})
                    document.getElementById('imageDiv').innerHTML =  imgHtml;
                    console.log()
                }
            );

        }

    </script>
    <fb:login-button scope="public_profile,email" onlogin="checkLoginState();">
    </fb:login-button>
    <div class="container">
	<div class="row">
	  <div class="col-md-6">
	  <h1><span class="badge badge-light">Google SaaS Exercise</span></h1>
        <form action="<%= blobstoreService.createUploadUrl("/upload") %>" method="post" enctype="multipart/form-data">
           
              
              
              
              <div class="form-group files">
               
                <input type="file" name="myFile" class="form-control" multiple="">
              </div>
              
             <input type="submit" class="btn btn-info" value="Submit">
          </form>
	      
	      
	  </div>
	   
	</div>
</div>

    </body>
</html>