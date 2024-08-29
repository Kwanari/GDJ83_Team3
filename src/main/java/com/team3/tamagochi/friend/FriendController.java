package com.team3.tamagochi.friend;


import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.WebSocketSession;

import com.team3.tamagochi.mypet.MyPetDTO;
import com.team3.tamagochi.store.ItemDTO;
import com.team3.tamagochi.users.InventoryDTO;
import com.team3.tamagochi.users.UsersDTO;

@Controller
@RequestMapping("/friend/*")
public class FriendController {

	@Autowired
	private FriendService friendService;
	
	
	
	
	@GetMapping("friendList")
	public void getFriendList(UsersDTO usersDTO, Model model, HttpSession session) throws Exception{
		
		usersDTO = (UsersDTO) session.getAttribute("users_info");
		
		usersDTO = friendService.getFriendList(usersDTO);
		
		model.addAttribute("usersDTO", usersDTO);
		
		
		
	}
	
	@GetMapping("friendDetail")
	public String getFriendDetail(FriendDTO friendDTO, Model model, HttpSession session) throws Exception{
	
		friendDTO = friendService.getFriendID(friendDTO);
		UsersDTO myDTO = (UsersDTO) session.getAttribute("users_info");
		
		// 주소창에서 남의 친구 detail에 들어가는걸 방지하기 위한 검증용 if문
		// 클릭한 친구의 정보에 들어있는 user_id와 로그인한 사용자의 user_id가 일치하지 않으면 else문을 실행
		if(friendDTO.getUser_id().equals(myDTO.getUser_id())) {
			UsersDTO friendInfoDTO = friendService.getFriendDetail(friendDTO);
			model.addAttribute("friendInfoDTO", friendInfoDTO);
			model.addAttribute("myDTO", myDTO);
			
			List<ItemDTO> inventoryList  = friendService.getInvenList(myDTO);
			
			model.addAttribute("inventoryList", inventoryList);			
		}else {
			model.addAttribute("result", "잘못된 접근입니다.");
			model.addAttribute("url", "/friend/friendList");
			return "commons/message";
		}
		return "/friend/friendDetail";
	}
	
	@GetMapping("sendGift")
	public String getInvenList(AlarmDTO alarmDTO, InventoryDTO inventoryDTO, HttpSession session, Model model) throws Exception {
		UsersDTO usersDTO = (UsersDTO) session.getAttribute("users_info");
		
		// 유저 인벤토리 리스트
		InventoryDTO userInventoryDTO = new InventoryDTO();
		userInventoryDTO.setUser_id(usersDTO.getUser_id());
		userInventoryDTO.setItem_num(inventoryDTO.getItem_num());
		
		// 선물보낼 때 알림 보내기, 보내는 이의 아이디가 로그인한 유저 아이디
		alarmDTO.setAlarm_sender(usersDTO.getUser_id());
		
		int result = friendService.takeGift(inventoryDTO);
		
		result = friendService.sendGift(userInventoryDTO);
		if(result > 0) {
			result = friendService.sendAlarm(alarmDTO);
			model.addAttribute("result", "선물을 보냈습니다");
			model .addAttribute("url", "./friendList");
			
			
		}else {
			model.addAttribute("result", "오류발생.");
			model .addAttribute("url", "./friendList");
		}
		
		
		
		return "commons/message";
	}
	
	
	
	
	@GetMapping("makeFriend")
	public String addFriend(FriendDTO friendDTO, Model model, HttpSession session, MyPetDTO myPetDTO) throws Exception {
		// friend to user(친구가 유저를 추가)
		UsersDTO usersDTO = (UsersDTO) session.getAttribute("users_info");
		
		friendDTO.setFriend_id(usersDTO.getUser_id());
		int result = friendService.makeFriends(friendDTO);
		
		// user to friend(유저가 친구를 추가)
		FriendDTO friendDTOReversed = new FriendDTO();
		friendDTOReversed.setUser_id(usersDTO.getUser_id());
		friendDTOReversed.setFriend_id(friendDTO.getUser_id());
		result = friendService.makeFriends(friendDTOReversed);
		
		// 친구 불러오기
		//usersDTO = friendService.getFriendList(usersDTO);
		
		model.addAttribute("result", "친구가 되었어요");
		model.addAttribute("url", "/rank/rankDetail?pet_num=" + myPetDTO.getPet_num());
		return "commons/message";
	}
	
	@GetMapping("deleteFriend")
	public String deleteFriend(FriendDTO friendDTO, Model model, HttpSession session) throws Exception{
		// 친구가 유저를 삭제
		UsersDTO usersDTO = (UsersDTO) session.getAttribute("users_info");
		friendDTO.setFriend_id(usersDTO.getUser_id());
		int result = friendService.deleteFriend(friendDTO);
		
		// 유저가 친구를 삭제
		FriendDTO friendDTOReversed = new FriendDTO();
		friendDTOReversed.setUser_id(usersDTO.getUser_id());
		friendDTOReversed.setFriend_id(friendDTO.getUser_id());
		result = friendService.deleteFriend(friendDTOReversed);
		
		if(result>0) {
			model.addAttribute("result", "친구를 끊었습니다.");
			model .addAttribute("url", "./friendList");
		} else {
			model.addAttribute("result", "오류발생.");
			model .addAttribute("url", "./friendList");
		}
		return "commons/message";
	}
	
}
