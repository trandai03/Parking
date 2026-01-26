package com.project.parking.controller;

import com.project.parking.dto.EmployeeDTO;
import com.project.parking.dto.request.EmployeeRequest;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quản lý nhân viên", description = "APIs để quản lý thông tin nhân viên làm việc tại các bãi đỗ xe")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "Lấy danh sách tất cả nhân viên", description = "API này dùng để lấy danh sách tất cả các nhân viên trong hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees(@RequestParam(required = false) String name) {
        log.info("Fetching all employees");
        List<EmployeeDTO> employees = employeeService.getAllEmployees(name);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Lấy thông tin nhân viên theo ID", description = "API này dùng để lấy thông tin chi tiết của một nhân viên theo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên với ID đã cho")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) throws DataNotFoundException {
        log.info("Fetching employee with id: {}", id);
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Lấy thông tin nhân viên theo user", description = "API này dùng để lấy thông tin chi tiết của một nhân viên theo user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên với ID đã cho")
    })  
    @GetMapping("/user/{userId}")
    public ResponseEntity<EmployeeDTO> getEmployeeByUserId(@PathVariable Long userId) throws DataNotFoundException {
        log.info("Fetching employee with user id: {}", userId);
        EmployeeDTO employee = employeeService.getEmployeeByUserId(userId);
        return ResponseEntity.ok(employee);
    }
    @Operation(summary = "Lấy danh sách nhân viên theo bãi đỗ xe", description = "API này dùng để lấy danh sách các nhân viên làm việc tại một bãi đỗ xe cụ thể.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByParkingLot(@PathVariable Long parkingLotId) {
        log.info("Fetching employees for parking lot id: {}", parkingLotId);
        List<EmployeeDTO> employees = employeeService.getEmployeesByParkingLot(parkingLotId);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Lấy danh sách nhân viên theo trạng thái", description = "API này dùng để lấy danh sách các nhân viên theo trạng thái (ACTIVE, INACTIVE, v.v.).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByStatus(@PathVariable String status) {
        log.info("Fetching employees with status: {}", status);
        List<EmployeeDTO> employees = employeeService.getEmployeesByStatus(status);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Tạo nhân viên mới", description = "API này dùng để đăng ký nhân viên mới vào hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo nhân viên thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bãi đỗ xe để gắn với nhân viên")
    })
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody EmployeeRequest employeeRequest)
            throws Exception {
        EmployeeDTO createdEmployee = EmployeeDTO.fromEmployee(employeeService.createEmployee(employeeRequest));
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @Operation(summary = "Cập nhật thông tin nhân viên", description = "API này dùng để cập nhật thông tin của nhân viên đã đăng ký.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên với ID đã cho")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id, @RequestBody EmployeeRequest employeeRequest)
            throws Exception {
        log.info("Updating employee with id: {}", id);
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeRequest);
        return ResponseEntity.ok(updatedEmployee);
    }

    @Operation(summary = "Xóa nhân viên", description = "API này dùng để xóa nhân viên khỏi hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên với ID đã cho")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) throws DataNotFoundException {
        log.info("Deleting employee with id: {}", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}