<jsp:useBean id="cart" scope="request" type=""/>
<!DOCTYPE html>
<html>
<body>
<canvas id="canvas" width=500 height=500 style="background-color:#808080;">
</canvas>
<a id="download" download="myImage.jpg" href="" onclick="download_img(this);">Download to myImage.jpg</a>
<script type="text/javascript">
    var canvas = document.getElementById("canvas");
    var ctx = canvas.getContext("2d");
    var ox = canvas.width / 2;
    var oy = canvas.height / 2;
    ctx.font = "42px serif";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillStyle = "#800";
    ctx.fillRect(ox / 2, oy / 2, ox, oy);
    download_img = function(el) {
        var image = canvas.toDataURL("image/jpg");
        el.href = image;
    };
</script>
</body>
</html>