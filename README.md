# Pixiv API for Android

An PixivAPI reEncapsulated with android async http client

## Target SDK

Android SDK >=19

# Usage

The api client use async http client,so please use it in async mode.
When use it in new Thread, don't forget to <code>Looper.prepare();</code>

## Simple Usage

```Java
pixivOAuth oAuth=new pixivOAuth();
oAuth.authAsync(username,password, handler/*to handle after success*/,Context)

// do something while authenticating

//when success
pixivAPI api=new pixivAPI(oAuth);
```


# References


[android-async-http](https://github.com/loopj/android-async-http)   
[pixivpy](https://github.com/upbit/pixivpy)   

Demo completed with many codes on the Internet. Thanks for these guys' works! 
