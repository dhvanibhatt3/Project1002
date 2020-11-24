<!DOCTYPE html>
<html>
<head>
    <title>Facebook Login JavaScript Example</title>
    <script src="https://code.jquery.com/jquery-1.9.1.min.js"></script>
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
            appId      : '398807191268392',
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
                    imgHtml = imgHtml  +' <img name="upload" value="' + photo.images[2].id + '" src="' + photo.images[2].source + '" onclick=imgOnclick("' + photo.images[2].source + '") /> ';
                })})
                document.getElementById('imageDiv').innerHTML =  imgHtml;
            }
        );

    }

</script>

<fb:login-button scope="public_profile,email" onlogin="checkLoginState();">
</fb:login-button>

</body>
</html>

