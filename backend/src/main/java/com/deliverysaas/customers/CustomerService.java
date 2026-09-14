package com.deliverysaas.customers;
import java.util.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import com.deliverysaas.customers.domain.Customer; import com.deliverysaas.shared.error.*; import com.deliverysaas.shared.security.AuthPrincipal;
import com.deliverysaas.users.UserRepository; import com.deliverysaas.users.domain.*;
import com.deliverysaas.audit.AuditService;
@Service public class CustomerService{
 private final CustomerRepository customers;private final UserRepository users;private final AuditService audit;public CustomerService(CustomerRepository c,UserRepository u,AuditService a){customers=c;users=u;audit=a;}
 @Transactional public CustomerResponse create(UUID org,CreateCustomerRequest r){User u=users.findByIdAndOrganizationId(r.userId(),org).orElseThrow(()->new NotFoundException("User not found"));if(u.getRole()!=UserRole.CUSTOMER||u.getStatus()!=UserStatus.ACTIVE)throw new ConflictException("User must be an active customer");if(customers.existsByUserId(u.getId()))throw new ConflictException("User already has a customer profile");Customer c=new Customer(u);apply(c,r.phone(),r.address(),r.city(),r.state(),r.postalCode());customers.save(c);audit.record("CREATE","CUSTOMER",c.getId(),"Customer created");return CustomerResponse.from(c);}
 @Transactional(readOnly=true) public List<CustomerResponse> all(UUID org){return customers.findAllByUserOrganizationId(org).stream().map(CustomerResponse::from).toList();}
 @Transactional(readOnly=true) public CustomerResponse one(AuthPrincipal p,UUID id){Customer c=get(id,p.organizationId());ownerOrManager(p,c);return CustomerResponse.from(c);}
 @Transactional public CustomerResponse update(AuthPrincipal p,UUID id,UpdateCustomerRequest r){Customer c=get(id,p.organizationId());ownerOrManager(p,c);apply(c,r.phone(),r.address(),r.city(),r.state(),r.postalCode());audit.record("UPDATE","CUSTOMER",c.getId(),"Customer updated");return CustomerResponse.from(c);}
 @Transactional public void delete(UUID org,UUID id){Customer c=get(id,org);c.getUser().setStatus(UserStatus.INACTIVE);audit.record("DEACTIVATE","CUSTOMER",c.getId(),"Customer deactivated");}
 private Customer get(UUID id,UUID org){return customers.findByIdAndUserOrganizationId(id,org).orElseThrow(()->new NotFoundException("Customer not found"));}
 private void ownerOrManager(AuthPrincipal p,Customer c){if(p.role()!=UserRole.ADMIN&&p.role()!=UserRole.MANAGER&&!p.userId().equals(c.getUser().getId()))throw new ForbiddenException("You cannot access this customer");}
 private void apply(Customer c,String p,String a,String city,String state,String pc){c.setPhone(trim(p));c.setAddress(trim(a));c.setCity(trim(city));c.setState(trim(state));c.setPostalCode(trim(pc));}private String trim(String v){return v==null||v.isBlank()?null:v.trim();}
}
