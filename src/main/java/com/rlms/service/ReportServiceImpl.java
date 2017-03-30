package com.rlms.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.rlms.constants.AMCType;
import com.rlms.constants.RLMSConstants;
import com.rlms.constants.RlmsErrorType;
import com.rlms.constants.Status;
import com.rlms.contract.AMCDetailsDto;
import com.rlms.contract.ComplaintsDtlsDto;
import com.rlms.contract.SiteVisitDtlsDto;
import com.rlms.contract.TechnicianWiseReportDto;
import com.rlms.contract.UserMetaInfo;
import com.rlms.dao.BranchDao;
import com.rlms.dao.ComplaintsDao;
import com.rlms.dao.LiftDao;
import com.rlms.model.RlmsBranchCustomerMap;
import com.rlms.model.RlmsComplaintMaster;
import com.rlms.model.RlmsComplaintTechMapDtls;
import com.rlms.model.RlmsLiftAmcDtls;
import com.rlms.model.RlmsLiftCustomerMap;
import com.rlms.model.RlmsSiteVisitDtls;
import com.rlms.predicates.LiftPredicate;
import com.rlms.utils.DateUtils;
import com.rlms.utils.PropertyUtils;

@Service("ReportService")
public class ReportServiceImpl implements ReportService {

	@Autowired
	private BranchDao branchDao;
	
	@Autowired
	private LiftDao liftDao;
	
	@Audited
	private ComplaintsDao complaintsDao;
	
	@Autowired
	private ComplaintsService complaintService;
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<AMCDetailsDto> getAMCDetailsForLifts(AMCDetailsDto dto){
		List<AMCDetailsDto> listOFAMCDetails = new ArrayList<AMCDetailsDto>();
		List<Integer> listOFAllCustomersForBranch = new ArrayList<Integer>();
		List<Integer> listOfLiftsForAMCDtls = new ArrayList<Integer>();
		List<RlmsLiftCustomerMap> listOFApplicableLifts = new ArrayList<RlmsLiftCustomerMap>();
		if(null == dto.getListOfBranchCustomerMapId()){
			List<RlmsBranchCustomerMap> listOFAllCustoOfBranch = this.branchDao.getAllCustomersOfBranch(dto.getBranchCompanyMapId());
			for (RlmsBranchCustomerMap rlmsBranchCustomerMap : listOFAllCustoOfBranch) {
				listOFAllCustomersForBranch.add(rlmsBranchCustomerMap.getBranchCustoMapId());
			}			
		}
		
		if(null != dto.getLiftCustomerMapId() && !dto.getLiftCustomerMapId().isEmpty()){
			listOFApplicableLifts = this.liftDao.getAllLiftsByIds(dto.getLiftCustomerMapId());
		}else{
			if(null != dto.getListOfBranchCustomerMapId() && !dto.getListOfBranchCustomerMapId().isEmpty()){
				listOFApplicableLifts = this.liftDao.getAllLiftsForCustomres(dto.getListOfBranchCustomerMapId());
			}else{
				 listOFApplicableLifts = this.liftDao.getAllLiftsForCustomres(listOFAllCustomersForBranch);
			}
		}
		for (RlmsLiftCustomerMap rlmsLiftCustomerMap : listOFApplicableLifts) {
			listOfLiftsForAMCDtls.add(rlmsLiftCustomerMap.getLiftCustomerMapId());
		}
		
		List<RlmsLiftAmcDtls> listOfAMCDtls = this.liftDao.getAMCDetilsForLifts(listOfLiftsForAMCDtls, dto);
		Set<Integer> liftIds = new HashSet<Integer>();
		for (RlmsLiftAmcDtls liftAmcDtls : listOfAMCDtls) {
			liftIds.add(liftAmcDtls.getLiftCustomerMap().getLiftMaster().getLiftId());
		}
		
		for (Integer liftId : liftIds) {
			List<RlmsLiftAmcDtls> listForLift = new ArrayList<RlmsLiftAmcDtls>(listOfAMCDtls);
			CollectionUtils.filter(listForLift, new LiftPredicate(liftId));
			listOFAMCDetails.addAll(this.constructListOFAMcDtos(listOfAMCDtls));
		}
		
		
		
		return listOFAMCDetails;
	}
	
	private List<AMCDetailsDto> constructListOFAMcDtos(List<RlmsLiftAmcDtls> listOFAMCs){
		List<AMCDetailsDto> listOFDtos = new ArrayList<AMCDetailsDto>();
		int i = 0;
		for (RlmsLiftAmcDtls liftAmcDtls : listOFAMCs) {
			AMCDetailsDto dto  = new AMCDetailsDto();
			if(null != liftAmcDtls.getAmcEndDate()){
				dto.setAmcEndDate(DateUtils.convertDateToStringWithoutTime(liftAmcDtls.getAmcEndDate()));
			}
			if(null != liftAmcDtls.getAmcStartDate()){
				dto.setAmcStartDate(DateUtils.convertDateToStringWithoutTime(liftAmcDtls.getAmcStartDate()));
			}
			
			dto.setCustomerName(liftAmcDtls.getLiftCustomerMap().getBranchCustomerMap().getCustomerMaster().getCustomerName());
			if(null != liftAmcDtls.getAmcDueDate()){
				dto.setDueDate(DateUtils.convertDateToStringWithoutTime(liftAmcDtls.getAmcDueDate()));
			}
			
			if(null != liftAmcDtls.getAmcSlackStartDate()){
				dto.setLackEndDate(DateUtils.convertDateToStringWithoutTime(liftAmcDtls.getAmcSlackStartDate()));
			}
			
			if(null != liftAmcDtls.getAmcSlackEndDate()){
				dto.setLackEndDate(DateUtils.convertDateToStringWithoutTime(liftAmcDtls.getAmcSlackEndDate()));
			}
			
			dto.setLiftNumber(liftAmcDtls.getLiftCustomerMap().getLiftMaster().getLiftNumber());
			dto.setCity(liftAmcDtls.getLiftCustomerMap().getBranchCustomerMap().getCustomerMaster().getCity());
			dto.setArea(liftAmcDtls.getLiftCustomerMap().getBranchCustomerMap().getCustomerMaster().getArea());
			Date tempStartDate = listOFAMCs.get(listOFAMCs.size() - 1).getLiftCustomerMap().getLiftMaster().getAmcStartDate();
			Date tempEndDate = listOFAMCs.get(listOFAMCs.size() - 1).getLiftCustomerMap().getLiftMaster().getAmcEndDate();
			Date tempDateOfInstallation = listOFAMCs.get(listOFAMCs.size() - 1).getLiftCustomerMap().getLiftMaster().getDateOfInstallation();
			dto.setStatus(this.calculateAMCStatus(tempStartDate, tempEndDate, tempDateOfInstallation).getStatusMsg());
			dto.setAmcAmount(liftAmcDtls.getLiftCustomerMap().getLiftMaster().getAmcAmount());
			
			if(i > 0 ){
				
				Integer diffInDays = DateUtils.daysBetween(listOFAMCs.get(i).getAmcStartDate(), listOFAMCs.get(i - 1).getAmcEndDate());
				if(diffInDays > 0){
					Date slackStartDate = DateUtils.addDaysToDate(listOFAMCs.get(i - 1).getAmcEndDate(), 1);
					Date slackEndDate = DateUtils.addDaysToDate(listOFAMCs.get(i).getAmcStartDate(), -1);
					if(null != slackStartDate &&  null != slackEndDate){
						{
							dto.setSlackStartDate(DateUtils.convertDateToStringWithoutTime(slackStartDate));
							dto.setSlackEndDate(DateUtils.convertDateToStringWithoutTime(slackEndDate));
							dto.setSlackperiod(diffInDays);
						}
					}
				}
			}
			
			if(AMCType.COMPREHENSIVE.getId() == liftAmcDtls.getAmcType()){
				dto.setAmcTypeStr(AMCType.COMPREHENSIVE.getType());
			}else if(AMCType.NON_COMPREHENSIVE.getId() == liftAmcDtls.getAmcType()){
				dto.setAmcTypeStr(AMCType.NON_COMPREHENSIVE.getType());
			}else if(AMCType.ON_DEMAND.getId() == liftAmcDtls.getAmcType()){
				dto.setAmcTypeStr(AMCType.ON_DEMAND.getType());
			}else if(AMCType.OTHER.getId() == liftAmcDtls.getAmcType()){
				dto.setAmcTypeStr(AMCType.OTHER.getType());
			}
			listOFDtos.add(dto);
			i++;
		}
		return listOFDtos;
	}
	
	private Status calculateAMCStatus(Date amcStartDate, Date amcEndDate, Date dateOfInstallation){
		Status amcStatus = null;
		Date today = new Date();
		Date warrantyexpiryDate = DateUtils.addDaysToDate(dateOfInstallation, 365);
		Date renewalDate = DateUtils.addDaysToDate(amcEndDate, -30);
		if(DateUtils.isBeforeOrEqualToDate(amcEndDate, warrantyexpiryDate)){
			amcStatus = Status.UNDER_WARRANTY;
		}else if(DateUtils.isAfterOrEqualTo(renewalDate, today) && DateUtils.isBeforeOrEqualToDate(today, amcEndDate)){
			amcStatus = Status.RENEWAL_DUE;
		}else if(DateUtils.isAfterToDate(amcEndDate, today)){
			amcStatus = Status.AMC_PENDING;
		}else if(DateUtils.isBeforeOrEqualToDate(amcStartDate, today) &&  DateUtils.isAfterOrEqualTo(today, amcEndDate)){
			amcStatus = Status.UNDER_AMC;
		}
		return amcStatus;
		
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public String addAMCDetailsForLift(AMCDetailsDto dto, UserMetaInfo metaInfo) throws ParseException{
		RlmsLiftAmcDtls liftAmcDtls = this.constructLiftAMCDtls(dto, metaInfo);
		this.liftDao.saveLiftAMCDtls(liftAmcDtls);
		return PropertyUtils.getPrpertyFromContext(RlmsErrorType.ADDED_AMC_DTLS_SUCCESSFULLY.getMessage());
	}
	
	public RlmsLiftAmcDtls constructLiftAMCDtls(AMCDetailsDto dto, UserMetaInfo metaInfo) throws ParseException{
		RlmsLiftAmcDtls liftAMCDtls = new RlmsLiftAmcDtls();
		RlmsLiftCustomerMap liftCustomerMap = this.liftDao.getLiftCustomerMapById(dto.getLiftCustoMapId());
		
		liftAMCDtls.setActiveFlag(RLMSConstants.ACTIVE.getId());
		if(null != dto.getAmcEndDate()){
			liftAMCDtls.setAmcDueDate(DateUtils.addDaysToDate(dto.getAmcEdDate(), -30));
		}
		if(null != dto.getAmcEndDate()){
			liftAMCDtls.setAmcEndDate(dto.getAmcEdDate());
		}
		
		if(null != dto.getAmcStartDate()){
			liftAMCDtls.setAmcStartDate(dto.getAmcStDate());
		}
		
		if(null != liftCustomerMap){
			liftAMCDtls.setLiftCustomerMap(liftCustomerMap);
		}
		
		if(null != dto.getAmcStartDate() && null != dto.getAmcEndDate()){
			Status amcStatus = this.calculateAMCStatus(dto.getAmcStDate(), dto.getAmcEdDate(), liftCustomerMap.getLiftMaster().getDateOfInstallation());
			liftAMCDtls.setStatus(amcStatus.getStatusId());
			
		}
		liftAMCDtls.setAmcAmount(dto.getAmcAmount());
		liftAMCDtls.setAmcType(dto.getAmcType());
		liftAMCDtls.setUpdatedBy(metaInfo.getUserId());
		liftAMCDtls.setUpdatedDate(new Date());
		liftAMCDtls.setCreatedBy(metaInfo.getUserId());
		liftAMCDtls.setCraetedDate(new Date());
		
		return liftAMCDtls;
		
		
	}
	@Transactional(propagation = Propagation.REQUIRED)
	public List<TechnicianWiseReportDto> getSiteVisitReport(TechnicianWiseReportDto dto){
		List<RlmsComplaintTechMapDtls> listOfComplaints = this.complaintsDao.getListOfComplaintDtlsForTechies(dto);
		List<TechnicianWiseReportDto> listOfAllComplaints = new ArrayList<TechnicianWiseReportDto>();
		for (RlmsComplaintTechMapDtls rlmsComplaintTechMapDtls : listOfComplaints) {
			TechnicianWiseReportDto complaintwiseSiteVisitReport = new TechnicianWiseReportDto();
			List<SiteVisitDtlsDto> listOfAllVisists = new ArrayList<SiteVisitDtlsDto>();
			List<RlmsSiteVisitDtls> listOfAllVisits = this.complaintsDao.getAllVisitsForComnplaints(rlmsComplaintTechMapDtls.getComplaintTechMapId());
			if(null != rlmsComplaintTechMapDtls.getComplaintMaster().getRegistrationDate()){
				complaintwiseSiteVisitReport.setComplaintRegDate(DateUtils.convertDateToStringWithTime(rlmsComplaintTechMapDtls.getComplaintMaster().getRegistrationDate()));
			}
			
			if(null != rlmsComplaintTechMapDtls.getPlannedEndDate()){
				complaintwiseSiteVisitReport.setComplaintResolveDate(DateUtils.convertDateToStringWithTime(rlmsComplaintTechMapDtls.getPlannedEndDate()));
			}
			
			
			complaintwiseSiteVisitReport.setComplaintStatus(Status.getStringFromID(rlmsComplaintTechMapDtls.getStatus()));
			complaintwiseSiteVisitReport.setComplNumber(rlmsComplaintTechMapDtls.getComplaintMaster().getComplaintNumber());
			complaintwiseSiteVisitReport.setCustomerName(rlmsComplaintTechMapDtls.getComplaintMaster().getLiftCustomerMap().getBranchCustomerMap().getCustomerMaster().getCustomerName());
			complaintwiseSiteVisitReport.setLiftNumber(rlmsComplaintTechMapDtls.getComplaintMaster().getLiftCustomerMap().getLiftMaster().getLiftNumber());
			complaintwiseSiteVisitReport.setTechName(rlmsComplaintTechMapDtls.getUserRoles().getRlmsUserMaster().getFirstName() + " " + rlmsComplaintTechMapDtls.getUserRoles().getRlmsUserMaster().getLastName());
			complaintwiseSiteVisitReport.setTechNumber(rlmsComplaintTechMapDtls.getUserRoles().getRlmsUserMaster().getContactNumber());
			if(null != listOfAllVisists &&  !listOfAllVisists.isEmpty()){
				complaintwiseSiteVisitReport.setTotalNoOfVisits(listOfAllVisists.size());
			}
			Long totalTimeForComplaint = null;
			for (RlmsSiteVisitDtls rlmsSiteVisitDtls : listOfAllVisits) {
				SiteVisitDtlsDto siteVisitDto = new SiteVisitDtlsDto();
				siteVisitDto.setFromDateDtr(DateUtils.convertDateToStringWithTime(rlmsSiteVisitDtls.getFromDate()));
				siteVisitDto.setToDateStr(DateUtils.convertDateToStringWithTime(rlmsSiteVisitDtls.getToDate()));
				String totalTime = DateUtils.convertTimeIntoDaysHrMin(DateUtils.getDateDiff(rlmsSiteVisitDtls.getFromDate(), rlmsSiteVisitDtls.getToDate(), TimeUnit.SECONDS), TimeUnit.SECONDS);
				if(null != totalTime){
					siteVisitDto.setTotalTime(totalTime);
				}
				totalTimeForComplaint = DateUtils.getDateDiff(rlmsSiteVisitDtls.getFromDate(), rlmsSiteVisitDtls.getToDate(), TimeUnit.SECONDS);
				
				listOfAllVisists.add(siteVisitDto);
			}
			if(null != totalTimeForComplaint){
				complaintwiseSiteVisitReport.setTotalTimeTaken(DateUtils.convertTimeIntoDaysHrMin(totalTimeForComplaint, TimeUnit.SECONDS));
			}
			complaintwiseSiteVisitReport.setListOFAllVisits(listOfAllVisists);
			complaintwiseSiteVisitReport.setUserRating(rlmsComplaintTechMapDtls.getUserRating());
			listOfAllComplaints.add(complaintwiseSiteVisitReport);
		}
		return listOfAllComplaints;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<TechnicianWiseReportDto> getTechnicianWiseREport(TechnicianWiseReportDto dto){
		List<RlmsComplaintTechMapDtls> listOfComplaints = this.complaintsDao.getListOfComplaintDtlsForTechies(dto);
		List<TechnicianWiseReportDto> listOfAllComplaints = new ArrayList<TechnicianWiseReportDto>();
		for (RlmsComplaintTechMapDtls rlmsComplaintTechMapDtls : listOfComplaints) {
			TechnicianWiseReportDto complaintwiseSiteVisitReport = new TechnicianWiseReportDto();
			List<SiteVisitDtlsDto> listOfAllVisists = new ArrayList<SiteVisitDtlsDto>();
			List<RlmsSiteVisitDtls> listOfAllVisits = this.complaintsDao.getAllVisitsForComnplaints(rlmsComplaintTechMapDtls.getComplaintTechMapId());
					
			
			
			
			complaintwiseSiteVisitReport.setTechName(rlmsComplaintTechMapDtls.getUserRoles().getRlmsUserMaster().getFirstName() + " " + rlmsComplaintTechMapDtls.getUserRoles().getRlmsUserMaster().getLastName());
			complaintwiseSiteVisitReport.setTechNumber(rlmsComplaintTechMapDtls.getUserRoles().getRlmsUserMaster().getContactNumber());
			if(null != listOfAllVisists &&  !listOfAllVisists.isEmpty()){
				complaintwiseSiteVisitReport.setTotalNoOfVisits(listOfAllVisists.size());
			}
			Long totalTimeForComplaint = null;
			for (RlmsSiteVisitDtls rlmsSiteVisitDtls : listOfAllVisits) {
				SiteVisitDtlsDto siteVisitDto = new SiteVisitDtlsDto();
				siteVisitDto.setFromDateDtr(DateUtils.convertDateToStringWithTime(rlmsSiteVisitDtls.getFromDate()));
				siteVisitDto.setToDateStr(DateUtils.convertDateToStringWithTime(rlmsSiteVisitDtls.getToDate()));
				String totalTime = DateUtils.convertTimeIntoDaysHrMin(DateUtils.getDateDiff(rlmsSiteVisitDtls.getFromDate(), rlmsSiteVisitDtls.getToDate(), TimeUnit.SECONDS), TimeUnit.SECONDS);
				if(null != totalTime){
					siteVisitDto.setTotalTime(totalTime);
				}
				totalTimeForComplaint = DateUtils.getDateDiff(rlmsSiteVisitDtls.getFromDate(), rlmsSiteVisitDtls.getToDate(), TimeUnit.SECONDS);
				
				listOfAllVisists.add(siteVisitDto);
			}
			if(null != totalTimeForComplaint){
				complaintwiseSiteVisitReport.setTotalTimeTaken(DateUtils.convertTimeIntoDaysHrMin(totalTimeForComplaint, TimeUnit.SECONDS));
			}
			complaintwiseSiteVisitReport.setListOFAllVisits(listOfAllVisists);
			complaintwiseSiteVisitReport.setUserRating(rlmsComplaintTechMapDtls.getUserRating());
			listOfAllComplaints.add(complaintwiseSiteVisitReport);
		}
		return listOfAllComplaints;
	}
}