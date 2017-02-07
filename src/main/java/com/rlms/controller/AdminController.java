package com.rlms.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rlms.constants.RlmsErrorType;
import com.rlms.contract.AddNewUserDto;
import com.rlms.contract.BranchDtlsDto;
import com.rlms.contract.CompanyDtlsDTO;
import com.rlms.contract.CustomerDtlsDto;
import com.rlms.contract.LiftDtlsDto;
import com.rlms.contract.RegisterDto;
import com.rlms.contract.ResponseDto;
import com.rlms.contract.UserDtlsDto;
import com.rlms.contract.UserRoleDtlsDTO;
import com.rlms.exception.ExceptionCode;
import com.rlms.exception.RunTimeException;
import com.rlms.exception.ValidationException;
import com.rlms.model.RlmsBranchMaster;
import com.rlms.model.RlmsCompanyBranchMapDtls;
import com.rlms.model.RlmsCompanyMaster;
import com.rlms.model.RlmsSpocRoleMaster;
import com.rlms.model.RlmsUsersMaster;
import com.rlms.service.CompanyService;
import com.rlms.service.CustomerService;
import com.rlms.service.LiftService;
import com.rlms.service.UserService;
import com.rlms.utils.PropertyUtils;

@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController{
	
	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private CustomerService customerService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private LiftService liftService;
	
	private static final Logger logger = Logger.getLogger(AdminController.class);
	
	 @RequestMapping(value = "/addNewCompany", method = RequestMethod.POST)
	    public @ResponseBody ResponseDto addNewCompany(@RequestBody CompanyDtlsDTO companyDtlsDTO) throws RunTimeException, ValidationException {
	        System.out.println("Adding n ew Company");
	        ResponseDto reponseDto = new ResponseDto();
	        try{
	        	logger.info("In addNewCompany method");
	        	reponseDto.setResponseMessage(this.companyService.validateAndSaveCompanyObj(companyDtlsDTO, this.getMetaInfo()));
	        	
	        }catch(ValidationException vex){
	        	logger.error(ExceptionUtils.getFullStackTrace(vex));	        	
	        	throw vex;
	        }catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));	       	
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return reponseDto;
	    }
	 
	 @RequestMapping(value = "/getApplicableRoles", method = RequestMethod.POST)
	    public @ResponseBody List<RlmsSpocRoleMaster> getApplicableRoles() throws RunTimeException {
	        List<RlmsSpocRoleMaster> listOfAllRoles = null;
	        
	        try{
	        	logger.info("Method :: getAllActiveRoles");
	        	listOfAllRoles =  this.userService.getAllRoles(this.getMetaInfo());
	        	
	        }catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return listOfAllRoles;
	 }
	 
	 @RequestMapping(value = "/getAllUsersForCompany", method = RequestMethod.POST)
	    public @ResponseBody List<UserDtlsDto> getAllUsersForCompany(@RequestBody CompanyDtlsDTO companyDtlsDTO) throws RunTimeException {
	        List<UserDtlsDto> listOfAllUsers = null;
	        
	        try{
	        	logger.info("Method :: getAllUsersForCompany");
	        	listOfAllUsers =  this.userService.getAllUsersForCompany(companyDtlsDTO.getCompanyId());
	        	
	        }catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return listOfAllUsers;
	    }
	 
	 @RequestMapping(value = "/getAllRegisteredUsers", method = RequestMethod.POST)
	    public @ResponseBody List<UserDtlsDto> getAllRegisteredUsers(UserDtlsDto dto) throws RunTimeException {
	        List<UserDtlsDto> listOfAllUsers = null;
	        
	        try{
	        	logger.info("Method :: getAllRegisteredUsers");
	        	listOfAllUsers =  this.userService.getAllRegisteredUsers(dto.getCompanyId(), this.getMetaInfo());
	        	
	        }catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return listOfAllUsers;
	    }
	

	 @RequestMapping(value = "/getAllApplicableCompanies", method = RequestMethod.POST)
	    public @ResponseBody List<RlmsCompanyMaster> getAllApplicableCompanies() throws RunTimeException {
	        List<RlmsCompanyMaster> listOfApplicableCompanies = null;
	        
	        try{
	        	logger.info("Method :: getAllApplicableCompanies");
	        	listOfApplicableCompanies =  this.companyService.getAllCompanies(this.getMetaInfo());
	        	
	        }catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return listOfApplicableCompanies;
	    }
	 //Thgis function will return details of each company to show on company page table
	 @RequestMapping(value = "/getAllCompanyDetails", method = RequestMethod.POST)
	    public @ResponseBody List<CompanyDtlsDTO> getAllCompanyDetails() throws RunTimeException {
	        List<CompanyDtlsDTO> listOfApplicableCompaniesDetails = null;
	        
	        try{
	        	logger.info("Method :: getAllCompanyDetails");
	        	listOfApplicableCompaniesDetails =  this.companyService.getAllCompanyDetails(this.getMetaInfo());
	        	
	        }catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return listOfApplicableCompaniesDetails;
	    }
	 
	 @RequestMapping(value = "/assignRole", method = RequestMethod.POST)
	    public @ResponseBody ResponseDto assignRole(@RequestBody UserRoleDtlsDTO userRoleDtlsDTO) throws RunTimeException, ValidationException {
	        
		 	ResponseDto reponseDto = new ResponseDto();
	        try{
	        	logger.info("Method :: assignRole");
	        	reponseDto.setResponseMessage(this.userService.validateAndAssignRole(userRoleDtlsDTO, this.getMetaInfo()));
	        	
	        }catch(ValidationException vex){
	        	logger.error(ExceptionUtils.getFullStackTrace(vex));
	        	throw vex;
	        }
	        catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return reponseDto;
	    }
	 
	 @RequestMapping(value = "/getAllBranchesForCompany", method = RequestMethod.POST)
	    public @ResponseBody List<RlmsCompanyBranchMapDtls> getAllBranchesForCompany(@RequestBody CompanyDtlsDTO companyDtlsDTO) throws RunTimeException {
	        List<RlmsCompanyBranchMapDtls> listOfAllBranches = null;
	        
	        try{
	        	logger.info("Method :: getAllBranchesForCompany");
	        	listOfAllBranches =  this.companyService.getAllBranches(companyDtlsDTO.getCompanyId());
	        	
	        }catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return listOfAllBranches;
	    }
	 
	 @RequestMapping(value = "/registerUser", method = RequestMethod.POST)
	    public @ResponseBody ResponseDto registerUser(@RequestBody RegisterDto registerDto) throws RunTimeException {
		 	ResponseDto reponseDto = new ResponseDto();
	        
	        try{
	        	logger.info("Method :: registerUser");
	        	reponseDto.setResponseMessage(this.userService.registerUser(registerDto));
	        	
	        }catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return reponseDto;
	  }
	 
	 @RequestMapping(value = "/validateAndRegisterNewUser", method = RequestMethod.POST)
	    public @ResponseBody ResponseDto validateAndRegisterNewUser(@RequestBody AddNewUserDto dto) throws RunTimeException, ValidationException {
		 ResponseDto reponseDto = new ResponseDto();
	        
	        try{
	        	logger.info("Method :: validateAndRegisterNewUser");
	        	reponseDto.setResponseMessage(this.userService.validateAndRegisterNewUser(dto, this.getMetaInfo()));
	        	
	        }catch(ValidationException vex){
	        	logger.error(ExceptionUtils.getFullStackTrace(vex));
	        	throw vex;
	        }
	        catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return reponseDto;
	  }
	 
	 
	 @RequestMapping(value = "/validateAndRegisterNewCustomer", method = RequestMethod.POST)
	    public @ResponseBody ResponseDto validateAndRegisterNewCustomer(@RequestBody CustomerDtlsDto dto) throws RunTimeException, ValidationException {
		 ResponseDto reponseDto = new ResponseDto();
	        
	        try{
	        	logger.info("Method :: validateAndRegisterNewCustomer");
	        	reponseDto.setResponseMessage(this.customerService.validateAndRegisterNewCustomer(dto, this.getMetaInfo()));
	        	
	        }catch(ValidationException vex){
	        	logger.error(ExceptionUtils.getFullStackTrace(vex));
	        	throw vex;
	        }
	        catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return reponseDto;
	  }
	 
	 
	 @RequestMapping(value = "/addNewBranchInCompany", method = RequestMethod.POST)
	 public @ResponseBody ResponseDto addNewBranchInCompany(@RequestBody BranchDtlsDto dto) throws RunTimeException{
		 ResponseDto reponseDto = new ResponseDto();
	        
	        try{
	        	logger.info("Method :: addNewBranchInCompany");
	        	reponseDto.setResponseMessage(this.companyService.validateAndAddNewBranchInCompany(dto, this.getMetaInfo()));
	        	
	        }catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return reponseDto;
	 }
	 
	 @RequestMapping(value = "/getListOfBranchDtls", method = RequestMethod.POST)
	 public @ResponseBody List<BranchDtlsDto> getListOfBranchDtls(BranchDtlsDto dto) throws RunTimeException{
		 List<BranchDtlsDto> listOfBranches = null;
	        
	        try{
	        	logger.info("Method :: getListOfBranchDtls");
	        	listOfBranches = this.companyService.getListOfBranchDtls(dto.getCompanyId(), this.getMetaInfo());
	        	
	        }catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        	
	        }
	 
	        return listOfBranches;
	 }
	 
	 @RequestMapping(value = "/getListOfCustomerDtls", method = RequestMethod.POST)
	 public @ResponseBody List<CustomerDtlsDto> getListOfCustomerDtls(@RequestBody CustomerDtlsDto customerDtlsDto) throws RunTimeException {
		 List<CustomerDtlsDto> listOfCustomers = null;
	        
	        try{
	        	logger.info("Method :: getListOfCustomerDtls");
	        	listOfCustomers = this.customerService.getAllApplicableCustomers(customerDtlsDto, this.getMetaInfo());
	        	
	        }catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        	
	        }
	 
	        return listOfCustomers;
	 }
	 
	 @RequestMapping(value = "/validateAndRegisterNewLift", method = RequestMethod.POST)
	    public @ResponseBody ResponseDto validateAndRegisterNewLift(@RequestBody LiftDtlsDto dto) throws RunTimeException, ValidationException {
		 ResponseDto reponseDto = new ResponseDto();
	        
	        try{
	        	logger.info("Method :: validateAndRegisterNewCustomer");
	        	reponseDto.setResponseMessage(this.liftService.validateAndAddNewLiftDtls(dto, this.getMetaInfo()));
	        	
	        }
	        catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return reponseDto;
	  }
	 
	 @RequestMapping(value = "/getLiftsToBeApproved", method = RequestMethod.POST)
	    public @ResponseBody List<LiftDtlsDto> getLiftsToBeApproved() throws RunTimeException{
		 List<LiftDtlsDto> listOfInActiveLifts = new ArrayList<LiftDtlsDto>();
	        
	        try{
	        	logger.info("Method :: getLiftsToBeApproved");
	        	listOfInActiveLifts = this.liftService.getLiftsToBeApproved();
	        	
	        }
	        catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return listOfInActiveLifts;
	  }
	 
	 @RequestMapping(value = "/validateAndApproveLift", method = RequestMethod.POST)
	    public @ResponseBody ResponseDto validateAndApproveLift(@RequestBody LiftDtlsDto dto) throws RunTimeException, ValidationException {
		 ResponseDto reponseDto = new ResponseDto();
	        
	        try{
	        	logger.info("Method :: validateAndApproveLift");
	        	reponseDto.setResponseMessage(this.liftService.approveLift(dto, this.getMetaInfo()));
	        	
	        }
	        catch(Exception e){
	        	logger.error(ExceptionUtils.getFullStackTrace(e));
	        	throw new RunTimeException(ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(), PropertyUtils.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS.getMessage()));
	        }
	 
	        return reponseDto;
	  }
	 
	
}
