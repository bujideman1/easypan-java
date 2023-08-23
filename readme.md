easypan
# 所需配件
* redis
* nginx
* ffmpeg
* mysql


# git代理设置
设置代理：
```shell
git config --global http.proxy http://127.0.0.1:7890
git config --global https.proxy https://127.0.0.1:7890
```
取消代理
```shell
git config --global --unset http.proxy
git config --global --unset https.proxy
```