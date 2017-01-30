package com.rlms.dao;

import java.util.List;

import com.rlms.model.RlmsBranchCustomerMap;
import com.rlms.model.RlmsBranchMaster;
import com.rlms.model.RlmsCompanyBranchMapDtls;

public interface BranchDao {

	public List<RlmsCompanyBranchMapDtls> getAllBranches(Integer companyId);
	public Integer saveBranchM(RlmsBranchMaster rlmsBranchMaster);
	
	public Integer saveCompanyBranchMapDtls(RlmsCompanyBranchMapDtls companyBranchMapDtls);
	
	public RlmsBranchMaster getBranchByBranchName(String branchName);
	public List<RlmsBranchCustomerMap> getAllCustomersOfBranch(Integer commpBranchMapId);
	public List<RlmsCompanyBranchMapDtls> getAllBranchesForCopanies(List<Integer> ListOfCompanyIds);
	public RlmsCompanyBranchMapDtls getCompanyBranchMapDtls(Integer compBranchMapId);
}
