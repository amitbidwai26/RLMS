package com.rlms.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rlms.constants.RlmsErrorType;
import com.rlms.contract.AMCDetailsDto;
import com.rlms.contract.CompanyDtlsDTO;
import com.rlms.contract.ComplaintsDtlsDto;
import com.rlms.contract.ComplaintsDto;
import com.rlms.contract.CustomerDtlsDto;
import com.rlms.contract.LiftDtlsDto;
import com.rlms.dao.ComplaintsDao;
import com.rlms.dao.LiftDao;
import com.rlms.exception.ExceptionCode;
import com.rlms.exception.RunTimeException;
import com.rlms.exception.ValidationException;
import com.rlms.model.RlmsCompanyBranchMapDtls;
import com.rlms.model.RlmsComplaintTechMapDtls;
import com.rlms.model.RlmsLiftCustomerMap;
import com.rlms.model.RlmsSiteVisitDtls;
import com.rlms.service.CompanyService;
import com.rlms.service.DashboardService;
import com.rlms.utils.PropertyUtils;

@Controller
@RequestMapping("/dashboard")
public class DashBoardController extends BaseController {

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private CompanyService companyService;
	@Autowired
	private LiftDao liftDao;
	
	@Autowired
	ComplaintsDao complaintsDao;

	private static final Logger logger = Logger
			.getLogger(ComplaintController.class);

	@RequestMapping(value = "/getAMCDetails", method = RequestMethod.POST)
	public @ResponseBody
	List<AMCDetailsDto> getAMCDetailsForDashboard(
			@RequestBody CompanyDtlsDTO companyDtlsDTO)
			throws RunTimeException, ValidationException {

		List<AMCDetailsDto> listOFAmcDtls = null;
		List<RlmsCompanyBranchMapDtls> listOfAllBranches = null;

		List<Integer> companyBranchIds = new ArrayList<>();

		try {
			logger.info("Method :: getAllBranchesForCompany");
			listOfAllBranches = this.companyService
					.getAllBranches(companyDtlsDTO.getCompanyId());
			for (RlmsCompanyBranchMapDtls companyBranchMap : listOfAllBranches) {
				companyBranchIds.add(companyBranchMap.getCompanyBranchMapId());
			}

			List<CustomerDtlsDto> allCustomersForBranch = dashboardService
					.getAllCustomersForBranch(companyBranchIds);
			List<Integer> liftCustomerMapIds = new ArrayList<>();
			for (CustomerDtlsDto customerDtlsDto : allCustomersForBranch) {
				LiftDtlsDto dto = new LiftDtlsDto();
				dto.setBranchCustomerMapId(customerDtlsDto
						.getBranchCustomerMapId());
				List<RlmsLiftCustomerMap> list=dashboardService.getAllLiftsForBranchsOrCustomer(dto);
				for (RlmsLiftCustomerMap rlmsLiftCustomerMap : list) {
					liftCustomerMapIds.add(rlmsLiftCustomerMap
							.getLiftCustomerMapId());
				}
			}
			listOFAmcDtls = this.dashboardService
					.getAMCDetailsForDashboard(liftCustomerMapIds);

		} catch (Exception e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
			throw new RunTimeException(
					ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(),
					PropertyUtils
							.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS
									.getMessage()));
		}
		return listOFAmcDtls;
	}

	@RequestMapping(value = "/getListOfComplaintsForDashboard", method = RequestMethod.POST)
	public @ResponseBody
	List<ComplaintsDto> getListOfComplaints(@RequestBody ComplaintsDtlsDto dto)
			throws RunTimeException {
		List<ComplaintsDto> listOfComplaints = null;
		List<RlmsCompanyBranchMapDtls> listOfAllBranches = null;

		List<Integer> companyBranchMapIds = new ArrayList<>();
		List<Integer> branchCustomerMapIds = new ArrayList<>();
		listOfAllBranches = this.companyService
				.getAllBranches(dto.getCompanyId());
		for (RlmsCompanyBranchMapDtls companyBranchMap : listOfAllBranches) {
			companyBranchMapIds.add(companyBranchMap.getCompanyBranchMapId());
		}

		List<CustomerDtlsDto> allCustomersForBranch = dashboardService
				.getAllCustomersForBranch(companyBranchMapIds);

		List<Integer> liftCustomerMapIds = new ArrayList<>();
		for (CustomerDtlsDto customerDtlsDto : allCustomersForBranch) {
			LiftDtlsDto dtoToGetLifts = new LiftDtlsDto();
			dtoToGetLifts.setBranchCustomerMapId(customerDtlsDto
					.getBranchCustomerMapId());
			List<RlmsLiftCustomerMap> list=dashboardService.getAllLiftsForBranchsOrCustomer(dtoToGetLifts);
			for (RlmsLiftCustomerMap rlmsLiftCustomerMap : list) {
				liftCustomerMapIds.add(rlmsLiftCustomerMap
						.getLiftCustomerMapId());
			}
		}

		dto.setListOfLiftCustoMapId(liftCustomerMapIds);
		try {
			logger.info("Method :: getListOfComplaints");
			listOfComplaints = this.dashboardService.getListOfComplaintsBy(dto);

		} catch (Exception e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
			throw new RunTimeException(
					ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(),
					PropertyUtils
							.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS
									.getMessage()));
		}

		return listOfComplaints;
	}
	
	@RequestMapping(value = "/getListOfComplaintsForSiteVisited", method = RequestMethod.POST)
	public @ResponseBody
	List<ComplaintsDto> getListOfComplaintsForSiteVisited(@RequestBody ComplaintsDtlsDto dto)
			throws RunTimeException {
		List<ComplaintsDto> listOfComplaints = null;
		List<RlmsCompanyBranchMapDtls> listOfAllBranches = null;
		Set<Integer> siteVisitedTodayComplaintIds=new HashSet<>();

		List<Integer> companyBranchMapIds = new ArrayList<>();
		List<Integer> branchCustomerMapIds = new ArrayList<>();
		listOfAllBranches = this.companyService
				.getAllBranches(dto.getCompanyId());
		for (RlmsCompanyBranchMapDtls companyBranchMap : listOfAllBranches) {
			companyBranchMapIds.add(companyBranchMap.getCompanyBranchMapId());
		}

		List<CustomerDtlsDto> allCustomersForBranch = dashboardService
				.getAllCustomersForBranch(companyBranchMapIds);

		List<Integer> liftCustomerMapIds = new ArrayList<>();
		for (CustomerDtlsDto customerDtlsDto : allCustomersForBranch) {
			LiftDtlsDto dtoToGetLifts = new LiftDtlsDto();
			dtoToGetLifts.setBranchCustomerMapId(customerDtlsDto
					.getBranchCustomerMapId());
			List<RlmsLiftCustomerMap> list=dashboardService.getAllLiftsForBranchsOrCustomer(dtoToGetLifts);
			for (RlmsLiftCustomerMap rlmsLiftCustomerMap : list) {
				liftCustomerMapIds.add(rlmsLiftCustomerMap
						.getLiftCustomerMapId());
			}
		}

		dto.setListOfLiftCustoMapId(liftCustomerMapIds);
		try {
			logger.info("Method :: getListOfComplaints");
			listOfComplaints = this.dashboardService.getListOfComplaintsBy(dto);
			List<Integer> listOfComplaintIds=new ArrayList<>();
			for (ComplaintsDto complaintsDto : listOfComplaints) {
				listOfComplaintIds.add(complaintsDto.getComplaintId());
			}
			Date todayDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			
			
			for (Integer complaintId : listOfComplaintIds) {
				RlmsComplaintTechMapDtls rlmsTechMapId=dashboardService.getComplTechMapObjByComplaintId(complaintId);
				if(rlmsTechMapId!=null){
					List<RlmsSiteVisitDtls> allVisits=dashboardService.getAllVisitsForComnplaints(rlmsTechMapId.getComplaintTechMapId());
					for (RlmsSiteVisitDtls rlmsSiteVisitDtls : allVisits) {
						if(sdf.format(rlmsSiteVisitDtls.getCreatedDate()).equals(sdf.format(todayDate))){
							siteVisitedTodayComplaintIds.add(complaintId);
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
			throw new RunTimeException(
					ExceptionCode.RUNTIME_EXCEPTION.getExceptionCode(),
					PropertyUtils
							.getPrpertyFromContext(RlmsErrorType.UNNKOWN_EXCEPTION_OCCHURS
									.getMessage()));
		}
		List<ComplaintsDto> finalListOfComplaints = new ArrayList<>();
		for (ComplaintsDto complaintsDto : listOfComplaints) {
			if(siteVisitedTodayComplaintIds.contains(complaintsDto.getComplaintId())){
				finalListOfComplaints.add(complaintsDto);
			} 
		}
 		return finalListOfComplaints;
	}
}