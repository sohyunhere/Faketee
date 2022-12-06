package com.konai.kurong.faketee.employee.service;

import com.konai.kurong.faketee.account.service.EmailAuthService;
import com.konai.kurong.faketee.corporation.entity.Corporation;
import com.konai.kurong.faketee.corporation.repository.CorporationRepository;
import com.konai.kurong.faketee.department.entity.Department;
import com.konai.kurong.faketee.department.repository.DepartmentRepository;
import com.konai.kurong.faketee.employee.dto.EmployeeUpdateRequestDto;
import com.konai.kurong.faketee.employee.dto.EmployeeReSendRequestDto;
import com.konai.kurong.faketee.employee.dto.EmployeeResponseDto;
import com.konai.kurong.faketee.employee.dto.EmployeeSaveRequestDto;
import com.konai.kurong.faketee.employee.entity.Employee;
import com.konai.kurong.faketee.employee.entity.EmployeeInfo;
import com.konai.kurong.faketee.employee.repository.EmployeeInfoRepository;
import com.konai.kurong.faketee.employee.repository.EmployeeRepository;
import com.konai.kurong.faketee.employee.utils.EmpRole;
import com.konai.kurong.faketee.position.entity.Position;
import com.konai.kurong.faketee.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeInfoRepository employeeInfoRepository;
    private final EmailAuthService emailAuthService;
    private final CorporationRepository corporationRepository;
    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;

    //    직원 저장하기
    @Transactional
    public void registerEmployee(Long corId, EmployeeSaveRequestDto requestDto) {
//        해당 회사의 관리자인지 권한 확인 필요

//        합류코드 만들기
        String joinCode = emailAuthService.createJoinCode();

//        직원 상세정보(EMP_INFO) Entity 만들어서 먼저 저장하기
        EmployeeInfo employeeInfo = EmployeeInfo.builder()
                .joinDate(new java.sql.Timestamp(requestDto.getJoinDate().getTime()).toLocalDateTime())
                .freeDate(new java.sql.Timestamp(requestDto.getJoinDate().getTime()).toLocalDateTime())
                .major(requestDto.getMajor())
                .cert(requestDto.getCert())
                .info(requestDto.getInfo())
                .empNo(requestDto.getEmpNo())
                .email(requestDto.getEmail())
                .joinCode(joinCode)
                .build();
        
        employeeInfoRepository.save(employeeInfo);

//        회사 합류코드 전송하기
        emailAuthService.sendJoinCode(employeeInfo.getEmail(), joinCode);

//        직원 만들기에 필요한 것들 불러오기
        EmpRole role = EmpRole.valueOf(requestDto.getRole());
        Corporation corporation = corporationRepository.findById(corId).orElseThrow();
        Position position = positionRepository.findById(requestDto.getPositionId()).orElseThrow();
        Department department = departmentRepository.findById(requestDto.getDepartmentId()).orElseThrow();

//       직원(EMP) Entity 만들기
        Employee employee = Employee.builder()
                .name(requestDto.getName())
                .role(role)
                .val("W")   //아직 합류코드 인증 전이므로 무조건 W
                .corporation(corporation)
                .user(null) //사용자가 합류코드 인증했을 때 userId 넣어줄거야
                .position(position)
                .department(department)
                .employeeInfo(employeeInfo)
                .build();

//        직원(EMP) Entity 저장하기
        employeeRepository.save(employee);
    }

    //    직원 수정하기
    @Transactional
    public void updateEmployee(Long corId, Long employeeId, EmployeeUpdateRequestDto requestDto) {
//        해당 회사의 관리자인지 권한 확인 필요

        //        직원 상세정보(EMP_INFO) Entity 만들어서 먼저 수정하기
        EmployeeInfo oldEmployeeInfo = findByEmployeeById(employeeId).getEmployeeInfo();

        EmployeeInfo newEmployeeInfo = EmployeeInfo.builder()
                .joinDate(new java.sql.Timestamp(requestDto.getJoinDate().getTime()).toLocalDateTime())
                .freeDate(new java.sql.Timestamp(requestDto.getJoinDate().getTime()).toLocalDateTime())
                .major(requestDto.getMajor())
                .cert(requestDto.getCert())
                .info(requestDto.getInfo())
                .empNo(requestDto.getEmpNo())
                .build();

        employeeInfoRepository.findById(oldEmployeeInfo.getId()).orElseThrow().update(newEmployeeInfo);

//        직원 수정에 필요한 것들 불러오기
        EmpRole role = EmpRole.valueOf(requestDto.getRole());
        Position position = positionRepository.findById(requestDto.getPositionId()).orElseThrow();
        Department department = departmentRepository.findById(requestDto.getDepartmentId()).orElseThrow();

//        수정한 직원(EMP) Entity 만들기
        Employee employee = Employee.builder()
                .name(requestDto.getName())
                .role(role)
                .position(position)
                .department(department)
                .build();

        //        직원(EMP) Entity 수정하기
        employeeRepository.findById(employeeId).orElseThrow().update(employee);
    }

    //    직원 비활성화
    @Transactional
    public void deactivateEmployee(Long corId, Long employeeId) {
//        해당 회사의 관리자인지 권한 확인 필요

        Employee employee = findByEmployeeById(employeeId);
        employee.deactivate();
    }

    //    employeeId로 Employee 반환
    public Employee findByEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId).orElseThrow();
    }

    //    employeeId로 Employee 반환
    public EmployeeInfo findByEmployeeInfoById(Long employeeInfoId) {
        return employeeInfoRepository.findById(employeeInfoId).orElseThrow();
    }

    public EmployeeResponseDto getEmployeeResponseDto(Long employeeId) {
        Employee employee = findByEmployeeById(employeeId);
        EmployeeInfo employeeInfo = findByEmployeeInfoById(employee.getEmployeeInfo().getId());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        EmployeeResponseDto employeeResponseDto = EmployeeResponseDto.builder()
                .id(employee.getId())
                .name(employee.getName())
                .role(employee.getRole().toString())
                .corporationId(employee.getCorporation().getId())
                .positionId(employee.getPosition().getId())
                .departmentId(employee.getDepartment().getId())
                .joinDate(simpleDateFormat.format(java.sql.Timestamp.valueOf(employeeInfo.getJoinDate())))
                .freeDate(simpleDateFormat.format(java.sql.Timestamp.valueOf(employeeInfo.getFreeDate())))
                .empNo(employeeInfo.getEmpNo())
                .major(employeeInfo.getMajor())
                .cert(employeeInfo.getCert())
                .info(employeeInfo.getInfo())
                .val(employee.getVal())
                .build();

        return employeeResponseDto;
    }

    //    직원 합류 초대 재전송
    public void reSendJoinCode(Long corId, Long employeeId, EmployeeReSendRequestDto requestDto) {
//        해당 회사의 관리자인지 권한 확인 필요

        Employee employee = findByEmployeeById(employeeId);
        EmployeeInfo employeeInfo = findByEmployeeInfoById(employee.getEmployeeInfo().getId());
        emailAuthService.sendJoinCode(requestDto.getEmail(), employeeInfo.getJoinCode());
    }
}