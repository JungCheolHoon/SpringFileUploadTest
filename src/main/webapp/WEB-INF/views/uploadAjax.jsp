<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">

<title>Insert title here</title>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.1/jquery.min.js"></script>
<script>
	function showImage(fileCallPath) {
		//alert(fileCallPath);

		$(".bigPictureWrapper").css("display", "flex").show();
		$(".bigPicture")
				.html(
						"<img src='/display?fileName="
								+ encodeURI(fileCallPath) + "'>").animate({
					width : '100%',
					height : '100%'
				}, 1000);
	}

	$(function() {

		var regex = new RegExp("(.*?)\.(exe|sh|jar|msi|com|php|jsp|asp)$");
		var maxSize = 5242880;

		function checkExtensions(fileName) {
			if (regex.test(fileName)) {
				alert("해당 종류의 파일은 업로드 하실 수 없습니다!")
				return false;
			}
			return true;
		}
		function checkFileSize(fileSize) {
			if (fileSize >= maxSize) {
				alert("업로드 하실 수 있는 최대 파일사이즈는 최대 5MB 입니다!");
				return false;
			}
			return true;
		}

		var cloneObj = $(".uploadDiv").clone();

		$("#uploadBtn").on("click", function(e) {
			var formData = new FormData();
			var inputFile = $("input[name='uploadFile']");
			var files = inputFile[0].files;
			var filesLength = files.length;

			for (var i = 0; i < filesLength; i++) {

				if (!checkExtensions(files[i].name)) {
					return false;
				}
				if (!checkFileSize(files[i].size)) {
					return false;
				}

				formData.append("uploadFile", files[i]);
			}

			$.ajax({
				url : "/uploadAjaxAction",
				processData : false, // 데이터 처리
				contentType : false, // MIME타입 (application/xml ...)
				data : formData, // 요청 시 전송 데이터
				type : "POST", // HTTP Method
				dataType : "json", // 응답 시 전송받는 데이터의 타입
				success : function(result) { // result : 응답데이터

					console.log(result);
					showUploadedFile(result);
					$(".uploadDiv").html(cloneObj.html());

				}
			});

		});

		var uploadResult = $(".uploadResult ul");

		function showUploadedFile(uploadResultArr) {
			var str = "";
			$(uploadResultArr)
					.each(
							function(i, obj) {

								if (!obj.image) {
									var fileCallPath = encodeURIComponent(obj.uploadPath
											+ "/"
											+ obj.uuid
											+ "_"
											+ obj.fileName);
									
									var fileLink = fileCallPath.replace(new RegExp(/\\/g),"/");
									
									str += "<li><div><a href='/download?fileName="
											+ fileCallPath
											+ "'>"
											+ "<img src='/resources/img/attach.png'>"
											+ obj.fileName +"</a>"+"<span data-file=\'" +fileCallPath+"\' data-type='file'> x </span>"
											+ "</div></li>";
								} else {
									//str += "<li>" + obj.fileName + "</li>";

									var fileCallPath = encodeURIComponent(obj.uploadPath
											+ "/thumb_"
											+ obj.uuid
											+ "_"
											+ obj.fileName);
									var originPath = obj.uploadPath + "/"
											+ obj.uuid + "_" + obj.fileName;

									originPath = originPath.replace(new RegExp(
											/\\/g), "/");

									str += "<li><a href=\"javascript:showImage(\'"
											+ originPath
											+ "\')\"><img src='/display?fileName="
											+ fileCallPath + "'></a>"+"<span data-file=\'"+fileCallPath + "\' data-type='image'> x </span>" 
											+"</li>";

								}

							});

			uploadResult.append(str);
		}
		
		$(".bigPictureWrapper").on("click", function(e) {
			$(".bigPicture").animate({
				width : '0%',
				height : '0%'
			}, 1000);
			setTimeout(function() {
				$(".bigPictureWrapper").hide();
			}, 1000);

		});
		
		$(".uploadResult").on("click","span",function(e){
			
			var targetFile = $(this).data("file");
			var type = $(this).data("type");
			
			$.ajax({
				url: '/deleteFile',
				data: {fileName: targetFile, type: type},
				dataType: "text",
				type: "POST",
				success: function(result){
					alert(result);
				}
			})
			
		})
		
	});
</script>
</head>
<body>

	<style>
.uploadResult {
	width: 100%;
	background-color: gray;
}

.uploadResult ul {
	display: flex;
	flex-flow: row;
	justify-content: center;
	align-items: center;
}

.uploadResult ul li {
	list-style: none;
	padding: 10px;
	align-content: center;
	text-align: center;
}

.uploadResult ul li img {
	width: 100px;
}

.uploadResult ul li span {
	color: white;
}

.bigPictureWrapper {
	position: absolute;
	display: none;
	justify-content: center;
	top: 0%;
	width: 100%;
	height: 100%;
	background-color: gray;
	z-index: 100;
	background: rgba(255, 255, 255, 0.5);
}

.bigPicture {
	position: relative;
	display: flex;
	justify-content: center;
	align-items: center;
}

.bigPicture img {
	width: 600px;
}
</style>

	<h1>Upload with Ajax</h1>
	<div class="uploadDiv">
		<input type="file" name="uploadFile" multiple />
	</div>
	<div class="uploadResult">
		<ul>
		</ul>
	</div>
	<div class="bigPictureWrapper">
		<div class="bigPicture"></div>
	</div>
	<div>
		<button id="uploadBtn">upload</button>
	</div>
</body>
</html>