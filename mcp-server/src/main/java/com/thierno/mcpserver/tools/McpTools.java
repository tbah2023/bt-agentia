package com.thierno.mcpserver.tools;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class McpTools {

    @McpTool(name = "getEmployee", description = "Retourne les informations complètes de l'employé avec son salaire son ancienneté")
    public Employee getEmployee(String name){
        Employee employee = getAllEmployees()
                .stream()
                .filter(e -> e.name.equalsIgnoreCase(name))
                .findFirst().orElse(null);
        if(employee == null) return new Employee("Inconnu",0,0);
        return employee;
    }

    @McpTool(description = "Retourne la liste complète de tous les employés avec leur salaire et ancienneté")
    public List<Employee> getAllEmployees(){
        return List.of(
                new Employee("Hassan",12300,4),
                new Employee("Mohamed",34000,1),
                new Employee("Imane",23000,10)
        );
    }
    public record Employee(String name, double salary, int seniority){}
}
