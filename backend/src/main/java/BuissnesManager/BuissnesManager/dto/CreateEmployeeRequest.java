package BuissnesManager.BuissnesManager.dto;

import java.util.List;

public class CreateEmployeeRequest {
    private String username;
    private String email;
    private String password;
    private String phone;
    private Long enterpriseId;
    private List<Long> moduleIds;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }

    public List<Long> getModuleIds() { return moduleIds; }
    public void setModuleIds(List<Long> moduleIds) { this.moduleIds = moduleIds; }
}
