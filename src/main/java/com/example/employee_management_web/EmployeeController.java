
package com.example.employee_management_web;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Controller
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/employees";
    }

    @GetMapping("/employees")
    public String listEmployees(Model model) {
        List<Employee> employees = employeeRepository.findAll();
        model.addAttribute("employees", employees);
        model.addAttribute("averageSalary", calculateAverageSalary(employees));
        return "employees/list";
    }

    @GetMapping("/employees/new")
    public String showAddForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("pageTitle", "Add Employee");
        return "employees/form";
    }

    @PostMapping("/employees/save")
    public String saveEmployee(
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "pageTitle",
                    employee.getId() == null ? "Add Employee" : "Edit Employee"
            );
            return "employees/form";
        }

        employeeRepository.save(employee);

        redirectAttributes.addFlashAttribute(
                "message", "Employee saved successfully!"
        );

        return "redirect:/employees";
    }

    @GetMapping("/employees/edit/{id}")
    public String showEditForm(
            @PathVariable Integer id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<Employee> employee = employeeRepository.findById(id);

        if (employee.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error", "Employee with ID " + id + " was not found."
            );
            return "redirect:/employees";
        }

        model.addAttribute("employee", employee.get());
        model.addAttribute("pageTitle", "Edit Employee");
        return "employees/form";
    }

    @PostMapping("/employees/delete/{id}")
    public String deleteEmployee(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        if (!employeeRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute(
                    "error", "Employee with ID " + id + " was not found."
            );
            return "redirect:/employees";
        }

        employeeRepository.deleteById(id);

        redirectAttributes.addFlashAttribute(
                "message", "Employee deleted successfully!"
        );

        return "redirect:/employees";
    }

    @GetMapping("/employees/search")
    public String searchEmployee(
            @RequestParam(required = false) Integer id,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute(
                    "error", "Please enter a valid employee ID."
            );
            return "redirect:/employees";
        }

        List<Employee> employees = employeeRepository.findById(id)
                .map(List::of)
                .orElseGet(List::of);

        model.addAttribute("employees", employees);
        model.addAttribute("averageSalary", calculateAverageSalary(employees));

        if (employees.isEmpty()) {
            model.addAttribute(
                    "error", "No employee found with ID " + id + "."
            );
        }

        return "employees/list";
    }

    private BigDecimal calculateAverageSalary(List<Employee> employees) {
        if (employees.isEmpty()) {
            return BigDecimal.ZERO.setScale(2);
        }

        BigDecimal total = employees.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(
                BigDecimal.valueOf(employees.size()),
                2,
                RoundingMode.HALF_UP
        );
    }
}
