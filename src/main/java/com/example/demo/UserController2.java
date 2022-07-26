package com.example.demo;

import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jwtlogin.claims.model.ClaimFlow;
import com.jwtlogin.claims.model.ClaimRequestApproval;
import com.jwtlogin.claims.model.ClaimStatus;
import com.jwtlogin.claims.model.ClaimType;
import com.jwtlogin.claims.model.Claims;
import com.jwtlogin.claims.model.ClaimsRequest;
import com.jwtlogin.claims.repository.ClaimRepository;
import com.jwtlogin.dao.UserRepository;
import com.jwtlogin.model.AuthResponse;
import com.jwtlogin.model.User;
import com.jwtlogin.model.Role;
import com.jwtlogin.model.Roles;
import com.jwtlogin.security.JwtTokenFilter;
import com.jwtlogin.security.JwtTokenUtil;


@RestController
@RequestMapping("/user")
public class UserController2 {
	
	//@GetMapping("/allusers")
	//public String displayUsers() {
	//	return "Display All Users";
	//}
	
	@Autowired
	ClaimRepository claimRepository;
	
	@Autowired
	JwtTokenFilter jwtTokenFilter;
	@Autowired
	JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	UserRepository userRepository;
	
	@GetMapping("/createclaims")
	@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
	public ResponseEntity<?> createClaims(HttpServletRequest request, @Valid @RequestBody ClaimsRequest claimsRequest ) {
		
		try {
			String token = jwtTokenFilter.getTokenFromRequest(request);
			
			if (token != null && jwtTokenUtil.validateJwtToken(token)) {
		
				Claims claims = new Claims();
				
				claims.setAmount(claimsRequest.getAmount());
				
				claims.setClaimStatus(ClaimStatus.CLAIM_CREATED);
				
				claims.setClaimFlow(ClaimFlow.CLAIM_TO_START);
				
				if (claimsRequest.getComment() != null) {
					claims.setComment(claimsRequest.getComment());
				}
				
				switch(claimsRequest.getClaimType()) {
				
				case "CONVEYANCE_CLAIM":
					claims.setClaim(ClaimType.CONVEYANCE_CLAIM);
					break;
					
				case "TELEPHONE_CLAIM":
					claims.setClaim(ClaimType.TELEPHONE_CLAIM);
					break;
					
				case "FUEL_CLAIM":
					claims.setClaim(ClaimType.FUEL_CLAIM);
					break;
					
				case "INTERNET_CLAIM":
					claims.setClaim(ClaimType.INTERNET_CLAIM);
					break;
				
				case "OTHERS":
					claims.setClaim(ClaimType.OTHERS);
					break;
					
				default:
					return ResponseEntity.badRequest().body("Invalid type of claim");
				}
				
				if (claims.getClaim() == ClaimType.OTHERS && claims.getComment() == null) {
					return ResponseEntity.badRequest().body("Comment required for Other Type");
				}
				
				if (claims.getAmount() == null) {
					return ResponseEntity.badRequest().body("Enter all the particulars");
				}
				
				String userNameString = jwtTokenUtil.getUserNameFromJwtToken(token);
				
				// Get Id from the Token Username
				User user =userRepository.findByUserName(userNameString).
						orElseThrow(() -> new UsernameNotFoundException("User name " + userNameString + "Not found. This should not have ended up here"));
				
				System.out.println("User ID "+ user.getId() + "User name "+ userNameString);
				
				claims.setId(user.getId());
				
				claimRepository.save(claims);
				
				return new ResponseEntity<Claims>(claims, HttpStatus.OK);
			}
		} catch (Exception e) {
			//logger.error("Cannot set user authentication: {}", e);
			throw new RuntimeException("Cannot set user authentication" + e.getMessage());
		}
		
		return new ResponseEntity<String>("Token not valid", HttpStatus.UNAUTHORIZED);
	}
	
	@GetMapping("/getSubmittedClaims")
	@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
	public ResponseEntity<?> submittedClaims(HttpServletRequest request) {
		
		try {
			String token = jwtTokenFilter.getTokenFromRequest(request);
			
			if (token != null && jwtTokenUtil.validateJwtToken(token)) {
				
				String userNameString = jwtTokenUtil.getUserNameFromJwtToken(token);
				
				// Get Id from the Token Username
				User user =userRepository.findByUserName(userNameString).
						orElseThrow(() -> 
						new UsernameNotFoundException("User name " + 
						userNameString + 
						"Not found. This should not have ended up here"));
				
				//ArrayList<Integer> idArrayList = new ArrayList<Integer>();
				
				//idArrayList.add(user.getId());
				
				//List<Claims>claimsItr = claimRepository.findAllById(idArrayList);
				List<Claims> claimsList = claimRepository.findByid(user.getId());
				
				System.out.println(userNameString + " " + user.getId() + " " + claimsList.size());
				
				if (claimsList.size() > 0) {
					
					for(Claims c :claimsList)
						PrintClaims(c);
					
					return new ResponseEntity<List<Claims>>(claimsList, HttpStatus.OK);
				}
				
				else {
					
					return new ResponseEntity<String>("No claims found", HttpStatus.OK);
				}
			}
		}
		catch (Exception e) {
			//logger.error("Cannot set user authentication: {}", e);
			throw new RuntimeException("Cannot set user authentication" + e.getMessage());
		}
		
		return new ResponseEntity<String>("List of Submitted Claims", HttpStatus.OK);
	}
	
	@GetMapping("/displayadmin")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String displayToAdmin() {
		return "Display only to admin";
	}
	
	@GetMapping("/getApprovalQueueOfClaims")
	@PreAuthorize("hasRole('ROLE_MAN_L1') or hasRole('ROLE_MAN_L2') or hasRole('ROLE_MAN_L3') or hasRole('ROLE_ADMIN')")
	public ResponseEntity<?> approvalQueueOfClaims(HttpServletRequest request) {
		
		try {
			String token = jwtTokenFilter.getTokenFromRequest(request);
			
			if (token != null && jwtTokenUtil.validateJwtToken(token)) {
				
				String userNameString = jwtTokenUtil.getUserNameFromJwtToken(token);
				
				// Get Id from the Token Username
				User user =userRepository.findByUserName(userNameString).
						orElseThrow(() -> 
						new UsernameNotFoundException("User name " + 
						userNameString + 
						"Not found. This should not have ended up here"));
				
				// Get the User Role (usually this is a HASH SET along with User role, ignore User role
				Set<Role> roles = user.getRoles();
				
				//Get the list of claims pending with specific User role
				
				for(Role r: roles) {
					
					
				
				if (r.getRoleName() == (Roles.ROLE_MAN_L1)) {
				
					//Return the list, List can be empty if nothing is present
					return new ResponseEntity<List<Claims>>(claimRepository.findByClaimFlow(ClaimFlow.CLAIM_TO_START), HttpStatus.OK);
				}
				else if (r.getRoleName() == (Roles.ROLE_MAN_L2)) {
					//Return the list, List can be empty if nothing is present
					return new ResponseEntity<List<Claims>>(claimRepository.findByClaimFlow(ClaimFlow.CLAIM_APPROVED_L1), HttpStatus.OK);
				}
				else if (r.getRoleName() == (Roles.ROLE_MAN_L3)) {
					//Return the list, List can be empty if nothing is present
					return new ResponseEntity<List<Claims>>(claimRepository.findByClaimFlow(ClaimFlow.CLAIM_APPROVED_L2), HttpStatus.OK);
				}
				else if (r.getRoleName() == (Roles.ROLE_ADMIN))
					//Return the list, List can be empty if nothing is present
					return new ResponseEntity<List<Claims>>(claimRepository.findByClaimFlow(ClaimFlow.CLAIM_UNKNOWN), HttpStatus.OK);
				}
				//else Traverse to see valid role
			
			}
			
		}catch (Exception e) {
			
			throw new RuntimeException("Cannot set user authentication" + e.getMessage());
		}
		
		//String returnString = "Username "+ userNameString + " is not valid to access";
		
		return new ResponseEntity<String>("No valid role identified to access", HttpStatus.OK);
	}
	
	@PostMapping("/updateClaimStatus")
	@PreAuthorize("hasRole('ROLE_MAN_L1') or hasRole('ROLE_MAN_L2') or hasRole('ROLE_MAN_L3') or hasRole('ROLE_ADMIN')")

	public ResponseEntity<?> UpdateClaimStatus(HttpServletRequest request, 
			@Valid @RequestBody ClaimRequestApproval claimRequestApproval ) {
		
		//Get the claim by ID
		
		List<Claims> claims = claimRepository.findByClaimId(claimRequestApproval.getClaimId());
		
		if (claims.size() != 1) {
			
			String errString = ("ClaimID" + claimRequestApproval.getClaimId() +  "is not valid");
			
			return new ResponseEntity<String>(errString, HttpStatus.NO_CONTENT);
		}
		Claims claimsUpdate = claims.get(0);
		//Check if the status is relevant to the user role
		
		try {
			String token = jwtTokenFilter.getTokenFromRequest(request);
			
			if (token != null && jwtTokenUtil.validateJwtToken(token)) {
				
				String userNameString = jwtTokenUtil.getUserNameFromJwtToken(token);
				
				// Get Id from the Token Username
				User user =userRepository.findByUserName(userNameString).
						orElseThrow(() -> 
						new UsernameNotFoundException("User name " + 
						userNameString + 
						"Not found. This should not have ended up here"));
				
				// Get the User Role (usually this is a HASH SET along with User role, ignore User role
				Set<Role> roles = user.getRoles();
				
				List<Roles> userRole = getListOfUserRole(roles);
				
				boolean updateStatus = false;
				
				boolean approvedStatus = false;
				//Get the list of claims pending with specific User role
				
				if (claimRequestApproval.getStatuString().equals("Approved")) {
					// Authority to Update
					
					approvedStatus = true;
					
				}
				else if (claimRequestApproval.getStatuString().equals( "Denied")) {
					
					approvedStatus = false;
				}
				else {
					
					String errString = "Invalid Claim Status = " 
							+ claimRequestApproval.getStatuString();
					System.out.println(errString);
					
					return new ResponseEntity<ClaimRequestApproval>(claimRequestApproval, HttpStatus.BAD_REQUEST);
				}
				
				
				if (claimsUpdate.getClaimStatus() == ClaimStatus.CLAIM_CREATED && 
						userRole.contains(Roles.ROLE_MAN_L1)) {
					
					updateStatus = true;
					
					claimsUpdate.setClaimStatus(ClaimStatus.CLAIM_INPROGRESS);
					
					if (approvedStatus) {
					
						claimsUpdate.setClaimFlow(ClaimFlow.CLAIM_APPROVED_L1);
					}
					else {
						claimsUpdate.setClaimFlow(ClaimFlow.CLAIM_DENIED_L1);
						claimsUpdate.setClaimStatus(ClaimStatus.CLAIM_DENIED);
					}
				}
				else if (claimsUpdate.getClaimStatus() == ClaimStatus.CLAIM_INPROGRESS &&
						claimsUpdate.getClaimFlow() == ClaimFlow.CLAIM_APPROVED_L1 &&
						userRole.contains(Roles.ROLE_MAN_L2)) {
					
					updateStatus = true;
					
					if (approvedStatus) {
					
						claimsUpdate.setClaimFlow(ClaimFlow.CLAIM_APPROVED_L2);
					}
					else {
						claimsUpdate.setClaimFlow(ClaimFlow.CLAIM_DENIED_L2);
						claimsUpdate.setClaimStatus(ClaimStatus.CLAIM_DENIED);
					}
				}	
				else if (claimsUpdate.getClaimStatus() == ClaimStatus.CLAIM_INPROGRESS &&
						claimsUpdate.getClaimFlow() == ClaimFlow.CLAIM_APPROVED_L2 &&
						userRole.contains(Roles.ROLE_MAN_L3)) {
					
					updateStatus = true;
					
					if (approvedStatus) {
					
						claimsUpdate.setClaimFlow(ClaimFlow.CLAIM_APPROVED_L3);
					}
					else {
						claimsUpdate.setClaimFlow(ClaimFlow.CLAIM_DENIED_L3);
						claimsUpdate.setClaimStatus(ClaimStatus.CLAIM_DENIED);
					}
				}	
				else {
					
					String errString = "Current role and WorkFlow doesnt allow Updation";
					
					System.out.println(errString);
					
					return new ResponseEntity<String>(errString, HttpStatus.FORBIDDEN);
				}
				
				if (updateStatus) {
					if (claimRequestApproval.getAckString() != null &&
							!claimRequestApproval.getAckString().isEmpty())
						claimsUpdate.setAckString(claimRequestApproval.getAckString());
					
					if(claimRequestApproval.getAckString() != null &&
							!claimRequestApproval.getCommentString().isEmpty())
						claimsUpdate.setComment(claimRequestApproval.getCommentString());
					
					PrintClaims(claimsUpdate);
					
					claimRepository.save(claimsUpdate);
					
					return new ResponseEntity<Claims>(claimsUpdate, HttpStatus.OK);
				}
			}
		}catch (Exception e) {
			
			throw new RuntimeException("Cannot set user authentication" + e.getMessage());
		}
		return new ResponseEntity<String>("Unexpected branch of code", HttpStatus.NOT_ACCEPTABLE);	
	}			
	
	private void PrintClaims(Claims c) {
		
		System.out.println("Claim id " + c.getClaimId());
		System.out.println("Ack String " + c.getAckString());
		System.out.println("Comment " + c.getComment());
		System.out.println("File Attached " + c.getFilename());
		System.out.println("Amount " + c.getAmount());
		System.out.println("Claim flow " + c.getClaimFlow());
		System.out.println("Claim Status " + c.getClaimStatus());
		System.out.println("Claim Type " + c.getClaimType());
		//System.out.println("Claim " + c.get);
		return;
	}
	
	private List<Roles> getListOfUserRole(Set<Role> roles) {
		
		List<Roles> roleList = new ArrayList<>();
		
		for (Role r: roles) {
			
			roleList.add(r.getRoleName());
			/*
			if (currentRole == Roles.ROLE_USER) {
				currentRole = tempRole;
			}
			else if (currentRole == Roles.ROLE_MAN_L1 && tempRole != Roles.ROLE_USER) {
				currentRole = tempRole;
			}
			else if (currentRole ==Roles.ROLE_MAN_L2 && (tempRole != Roles.ROLE_USER)
					&& (tempRole != Roles.ROLE_MAN_L1)) {
				currentRole = tempRole;
			}
			else if (currentRole == Roles.ROLE_MAN_L3 && tempRole == Roles.ROLE_ADMIN) {
				currentRole = tempRole;
			}
			//else if (currentRole == Roles.ROLE_ADMIN) {
			 //Dont update 	
			//}
			*/
		}
		return roleList;
	}
	//@Transactional(readOnly = true)
	//public List<Claims> getAllClaims(List<Integer> ids) {
	//	List<Claims> claimResponse = (List<Claims>) claimRepository.findAllById(ids);
	//	return claimResponse;
	//}

}
