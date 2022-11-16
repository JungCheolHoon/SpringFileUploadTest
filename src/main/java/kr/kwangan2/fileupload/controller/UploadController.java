package kr.kwangan2.fileupload.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kr.kwangan2.fileupload.domain.AttachFileDTO;
import lombok.extern.log4j.Log4j;
import net.coobird.thumbnailator.Thumbnailator;

@Controller
@Log4j
public class UploadController {

	// uploadAjax.jsp로 가는 메소드
	@GetMapping("/uploadAjax")
	public void uploadAjax() {

	}

	// 폴더제목을 오늘 날짜로 만드는 메소드
	private String getFolder() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String str = sdf.format(date);
		return str.replace("-", File.separator);
	}
	
	private boolean checkImageType(File file) {
		boolean flag = false;
		try {
			String contentType = Files.probeContentType(file.toPath());
			flag = contentType.startsWith("image");
		}catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return flag;
	}
	
	@PostMapping(value = "/uploadAjaxAction", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<List<AttachFileDTO>> uploadAjaxPost(MultipartFile[] uploadFile) {
		
		List<AttachFileDTO> list = new ArrayList<AttachFileDTO>();
		
		String uploadFolder = "c:/upload";
		File uploadPath = new File(uploadFolder, getFolder());
		
		if(uploadPath.exists() == false) {
			uploadPath.mkdirs();
		}
		
		for (MultipartFile multipartFile : uploadFile) {
			
			AttachFileDTO attachDTO = new AttachFileDTO();
			
			log.info("--------------------------------------------------");
			String originalFilename = multipartFile.getOriginalFilename();
			attachDTO.setFileName(originalFilename);
			log.info("upload file name : " + originalFilename);
			log.info("upload file size : " + multipartFile.getSize());
			log.info("getName"+ multipartFile.getName());
/*			
 			byte[] bytes = multipartFile.getBytes();
			int bytesLength = bytes.length;
			char[] chars = new char[bytesLength];
			String str = "";
			for(int i= 0 ; i < bytesLength; i++) {
				str += Integer.toBinaryString(bytes[i]);
			}
			log.info("getBytes"+ str);
*/
			try {
				log.info("getInputStream"+ multipartFile.getInputStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			log.info("isEmpty : " + multipartFile.isEmpty());
			log.info("--------------------------------------------------");

			originalFilename.substring(originalFilename.lastIndexOf("/") + 1);
			
			UUID uuid = UUID.randomUUID();
			
			originalFilename = uuid.toString() + "_" + originalFilename;
			
			File saveFile = new File(uploadPath, originalFilename);

			try {
				multipartFile.transferTo(saveFile);
				
				attachDTO.setUuid(uuid.toString());
				attachDTO.setUploadPath(getFolder());

				if(checkImageType(saveFile)) {
					FileOutputStream thumbnail = new FileOutputStream(new File(uploadPath,"thumb_"+originalFilename));
					Thumbnailator.createThumbnail(multipartFile.getInputStream(), thumbnail,100,100); // 파일, 파일 이름, 파일 픽셀크기
					attachDTO.setImage(true);
					thumbnail.close();
				}else {
					attachDTO.setImage(false);
				}
				
				list.add(attachDTO);
				
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} // for
		
		return new ResponseEntity<>(list,HttpStatus.OK);
	}
	
	@GetMapping("/display")
	@ResponseBody
	public ResponseEntity<byte[]> getFile(String fileName) {
		log.info("fileName: " + fileName);
		
		File file = new File("c:/upload/" + fileName);
		
		ResponseEntity<byte[]> result = null;
		
		HttpHeaders header = new HttpHeaders();
		try {
			header.add("content-Type", Files.probeContentType(file.toPath()));
			result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file),header,HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public ResponseEntity<Resource> downloadFile(@RequestHeader("User-Agent") String userAgent, String fileName){

		Resource resource = new FileSystemResource("c:/upload/"+fileName);
		
		if(resource.exists() == false) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		String resourceName = resource.getFilename();
		
		String resourceOriginalName = resourceName.substring(resourceName.indexOf("_") +1);
		
		HttpHeaders headers = new HttpHeaders();
		try {
			String downloadName = null;
			if(userAgent.contains("Trident")) {
				downloadName = URLEncoder.encode(resourceName,"UTF-8").replaceAll("/+", " ");
			}else if(userAgent.contains("Edge")){
				downloadName = URLEncoder.encode(resourceName,"UTF-8");
			}else {
				downloadName = new String(resourceName.getBytes("UTF-8"), "ISO-8859-1");
			}
			headers.add("Content-Dispositiom", "attachment; filename=" + downloadName);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<Resource>(resource,headers,HttpStatus.OK);
	}
	
	@PostMapping("/deleteFile")
	@ResponseBody
	public ResponseEntity<String> deleteFile(String fileName, String type){
		
		File file;
		
		try {
			file = new File("c:/upload/"+ URLDecoder.decode(fileName,"UTF-8"));
			file.delete();
			
			if(type.equals("image")) {
				String largeFileName = file.getAbsolutePath().replace("thumb_", "");
				file = new File(largeFileName);
				file.delete();
			}
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}	
		return new ResponseEntity<String>("deleted", HttpStatus.OK);
	}
	
}
