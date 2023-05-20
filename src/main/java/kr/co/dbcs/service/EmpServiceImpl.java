package kr.co.dbcs.service;

import kr.co.dbcs.domain.EmpDTO;
import kr.co.dbcs.domain.UsrDTO;
import kr.co.dbcs.util.LoginSHA;
import kr.co.dbcs.util.Validation;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static kr.co.dbcs.util.JdbcManager.*;

@Slf4j
public class EmpServiceImpl implements EmpService {

    private final Connection conn = MANAGER.getConnection();
    private final Statement stmt = MANAGER.getStatement();
    private PreparedStatement pstmt;
    private ResultSet rs;
    private EmpDTO empDTO;
    private UsrDTO usrDTO;

    public EmpServiceImpl(UsrDTO dto) throws SQLException {
        usrDTO = dto;
    }

    @Override
    public void empMenu() throws IOException, SQLException, ClassNotFoundException, NoSuchAlgorithmException {

        while (true) {
            BW.write("\n======================================================================\n");
            BW.write("|\t\t\t임직원근태관리 근로자 메뉴\t\t     |\n");
            BW.write("======================================================================\n");
            BW.write("|\t    1. 근무기록\t\t   |\t        2. 인적사항\t     |\n");
            BW.write("======================================================================\n");
            BW.write("|\t    3. 휴가신청\t\t    \t                  \t     |\n");
            BW.write("======================================================================\n");
            BW.write("|\t\t원하는 기능을 선택하세요.(0번 : 이전)\t\t     |\n");
            BW.write("======================================================================\n");
            BW.flush();

            switch (BR.readLine().trim()) {
                case "EXIT":
                case "exit":
                    BW.write("프로그램을 종료합니다.\n");
                    BW.flush();
                    MANAGER.closeConnection();
                    System.exit(0);
                    break;
                case "0":
                    BW.write("홈 화면으로 돌아갑니다.\n");
                    BW.flush();
                    return;
                case "1":
                    // 근무기록
                    BW.write("출퇴근 관리 근로자 메뉴로 이동합니다.\n");
                    BW.flush();
                    new AttServiceImpl(usrDTO.getUsrID()).attMenu();
                    break;
                case "2":
                    // 인적사항
                    showEmpInfo();
                    break;
                case "3":
                    // 휴가신청
                    new LeaveServiceImpl(usrDTO.getUsrID()).leaveEmp();
                    break;
                default:
                    BW.write("잘못된 입력입니다.\n");
                    BW.flush();
                    break;
            }
        }
    }

    @Override
    public void showEmpInfo() throws SQLException, IOException, ClassNotFoundException, NoSuchAlgorithmException {

        rs = stmt.executeQuery("SELECT * FROM EMP WHERE USRID = '" + usrDTO.getUsrID() + "'");

        if (empDTO == null) {
            empDTO = new EmpDTO();
            while (rs.next()) {
                empDTO.setUsrID(rs.getString("USRID"));
                empDTO.setName(rs.getString("NAME"));
                empDTO.setBirthDate(rs.getDate("BIRTHDATE"));
                empDTO.setGender(rs.getBoolean("GENDER"));
                empDTO.setContact(rs.getString("CONTACT"));
                empDTO.setHireDate(rs.getDate("HIREDATE"));
                empDTO.setSal(rs.getLong("SAL"));
                empDTO.setLeaveDay(rs.getByte("LEAVEDAY"));
                empDTO.setDeptCode(rs.getInt("DEPTCODE"));
                empDTO.setPosCode(rs.getInt("POSCODE"));
            }
        }

        while (true) {
            BW.write("사용자ID: " + empDTO.getUsrID());
            BW.write("\n이름: " + empDTO.getName());
            BW.write("\n생년월일: " + empDTO.getBirthDate());
            BW.write("\n성별: " + (empDTO.isGender() ? "남" : "여"));
            BW.write("\n연락처: " + empDTO.getContact());
            BW.write("\n입사일: " + empDTO.getHireDate());
            BW.write("\n기본급: " + empDTO.getSal());
            BW.write("\n잔여휴가: " + empDTO.getLeaveDay());
            BW.write("\n부서코드: " + empDTO.getDeptCode());
            BW.write("\n직급코드: " + empDTO.getPosCode() + "\n\n");
            BW.write("메뉴 입력(0: 이전 화면, 1: 인적사항 수정): ");
            BW.flush();

            switch (BR.readLine().trim()) {
                case "0":
                    // 근로자 홈
                    return;
                case "1":
                    // 인적사항 수정
                    updateEmpInfo();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void updateEmpInfo() throws SQLException, IOException, NoSuchAlgorithmException {

        BW.write("\n======================================================================\n");
        BW.write("|\t\t\t임직원근태관리 근로자 메뉴\t\t     |\n");
        BW.write("======================================================================\n");
        BW.write("|\t    1. 비밀번호 수정\t\t   |\t        2. 연락처 수정\t     |\n");
        BW.write("======================================================================\n");
        BW.write("|\t\t원하는 기능을 선택하세요.(0번 : 이전)\t\t     |\n");
        BW.write("======================================================================\n");
        BW.flush();

        switch (BR.readLine().trim()) {
            case "0":
                return;
            case "1":
                // 비밀번호 수정
                updatePw(usrDTO.getUsrID());
                break;
            case "2":
                // 연락처 수정
                updateContact(usrDTO.getUsrID());
                break;
            default:
                BW.write("잘못된 입력입니다.\n");
                BW.flush();
                break;
        }
    }

    @Override
    public void updatePw(String usrID) throws SQLException, IOException, NoSuchAlgorithmException {

        String salt = LoginSHA.Salt();

        String pw;
        String pw_decrypt;
        String dataPw = usrDTO.getPw();

        do {
            BW.write("현재 비밀번호를 입력하세요.: ");
            BW.flush();
            pw = BR.readLine().trim();

            pw_decrypt = LoginSHA.SHA512(pw, salt);
        } while (!pw_decrypt.equals(dataPw));

        while (true) {
            BW.write("새로운 비밀번호를 입력하세요. (비밀번호는 8자이상 영문, 숫자, 특수문자를 포함해야 합니다.): ");
            BW.flush();
            pw = BR.readLine().trim();

            if (!Validation.ValidatePw(pw)) {
                BW.write("비밀번호는 8자이상 영문, 숫자, 특수문자를 포함해야 합니다.\n");
                BW.flush();
            } else {
                String pw_encrypt = LoginSHA.SHA512(pw, salt);
                pstmt = conn.prepareStatement("UPDATE USR SET PW = ? WHERE USRID = '" + usrID + "'");
                pstmt.setString(1, pw_encrypt);
                pstmt.executeUpdate();
                break;
            }
        }
    }

    @Override
    public void updateContact(String usrID) throws SQLException, IOException {

        pstmt = conn.prepareStatement("UPDATE EMP SET CONTACT = ? WHERE USRID = '" + usrID + "'");
        BW.write("수정할 연락처를 입력하세요: ");
        BW.flush();
        pstmt.setString(1, BR.readLine().trim());
        pstmt.executeUpdate();
    }

    public void adminEmpMenu() throws IOException, SQLException {

        while (true) {
            BW.write("\n======================================================================\n");
            BW.write("|\t\t     임직원근태관리 관리자 메뉴\t\t\t     |\n");
            BW.write("======================================================================\n");
            BW.write("|\t    1. 부서이동\t\t   |\t        2. 직급관리\t     |\n");
            BW.write("======================================================================\n");
            BW.write("|\t    3. 급여관리\t\t   |\t                  \t     |\n");
            BW.write("======================================================================\n");
            BW.write("|\t\t원하는 기능을 선택하세요.(0번 : 이전)\t\t     |\n");
            BW.write("======================================================================\n");
            BW.flush();

            switch (BR.readLine().trim()) {
                case "0":
                    // 이전 화면
                    return;
                case "1":
                    // 부서 수정
                    updateDept();
                    break;
                case "2":
                    // 직급 수정
                    updatePos();
                    break;
                case "3":
                    // 기본급 수정
                    updateSal();
                    break;
                default:
                    BW.write("잘못된 입력입니다.\n\n");
                    break;
            }
        }
    }

    private void searchEmp() throws IOException, SQLException {

        BW.write("검색할 직원의 이름을 입력하세요.: ");
        BW.flush();
        String name = BR.readLine().trim();
        pstmt = conn.prepareStatement("SELECT * FROM emp WHERE name LIKE ?");
//        pstmt.setString(1, "'%" + name + "%'");
        pstmt.setString(1, "%" + name + "%");
        rs = pstmt.executeQuery();

        List<EmpDTO> empList = new ArrayList<>();
        while (rs.next()) {
            EmpDTO dto = new EmpDTO();
            dto.setUsrID(rs.getString(1));
            dto.setName(rs.getString(2));
            dto.setBirthDate(rs.getDate(3));
            dto.setGender(rs.getBoolean(4));
            dto.setContact(rs.getString(5));
            dto.setHireDate(rs.getDate(6));
            dto.setSal(rs.getInt(7));
            dto.setLeaveDay(rs.getByte(8));
            dto.setDeptCode(rs.getInt(9));
            dto.setPosCode(rs.getInt(10));
            empList.add(dto);
        }

        BW.write(empList + "\n");
    }

    private void updateDept() throws SQLException, IOException, NumberFormatException {

        searchEmp();
        BW.write("부서를 이동할 직원 ID를 입력하세요.: ");
        BW.flush();
        String usrID = BR.readLine().trim();
        BW.write("변경할 부서를 입력하세요: ");
        BW.flush();
        int deptCode = Integer.parseInt(BR.readLine().trim());
        pstmt = conn.prepareStatement("UPDATE EMP SET DEPTCODE = ? WHERE USRID = ?");
        pstmt.setInt(1, deptCode);
        pstmt.setString(2, usrID);
        int res = pstmt.executeUpdate();
        if (res > 0) BW.write(usrID + "님의 부서가 변경되었습니다.\n");
    }

    private void updatePos() throws SQLException, IOException {

        searchEmp();
        BW.write("직급을 변경할 직원 ID를 입력하세요.: ");
        BW.flush();
        String usrID = BR.readLine().trim();
        BW.write("변경할 직급을 입력하세요: ");
        BW.flush();
        int posCode = Integer.parseInt(BR.readLine().trim());
        pstmt = conn.prepareStatement("UPDATE EMP SET POSCODE = ? WHERE USRID = ?");
        pstmt.setInt(1, posCode);
        pstmt.setString(2, usrID);
        int res = pstmt.executeUpdate();
        if (res > 0) BW.write(usrID + "님의 직급이 변경되었습니다.\n");
    }

    private void updateSal() throws SQLException, IOException {

        searchEmp();
        BW.write("기본급을 변경할 직원 ID를 입력하세요.: ");
        BW.flush();
        String usrID = BR.readLine().trim();
        BW.write("변경할 기본급을 입력하세요: ");
        BW.flush();
        int sal = Integer.parseInt(BR.readLine().trim());
        pstmt = conn.prepareStatement("UPDATE EMP SET SAL = ? WHERE USRID = ?");
        pstmt.setInt(1, sal);
        pstmt.setString(2, usrID);
        int res = pstmt.executeUpdate();
        if (res > 0) BW.write(usrID + "님의 기본급이 변경되었습니다.\n");
    }
}
