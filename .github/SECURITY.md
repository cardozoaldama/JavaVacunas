# Security Policy

## Supported Versions

JavaVacunas is currently in alpha development. Security updates will be provided for the following versions:

| Version | Supported          |
| ------- | ------------------ |
| 0.1.0-alpha.x | :white_check_mark: |

Once the project reaches stable release (1.0.0), this policy will be updated to reflect long-term support versions.

## Reporting a Vulnerability

We take the security of JavaVacunas seriously. If you discover a security vulnerability, please follow these steps:

### Do Not

- Do not open a public GitHub issue for security vulnerabilities
- Do not discuss the vulnerability in public forums, social media, or mailing lists
- Do not exploit the vulnerability beyond what is necessary to demonstrate it

### Do

1. **Report privately** via one of these methods:
   - Email the maintainers (check repository for contact information)
   - Use GitHub's private vulnerability reporting feature if available
   - Open a draft security advisory on GitHub

2. **Include the following information**:
   - Description of the vulnerability
   - Steps to reproduce the issue
   - Affected versions
   - Potential impact assessment
   - Suggested remediation if you have one
   - Your contact information for follow-up

3. **Wait for acknowledgment** before public disclosure

### What to Expect

- **Initial Response**: Within 48 hours, we will acknowledge receipt of your report
- **Assessment**: Within 5 business days, we will provide an initial assessment of the issue
- **Updates**: We will keep you informed of our progress toward a fix
- **Resolution**: Security patches will be developed and tested
- **Disclosure**: Once a fix is available, we will coordinate disclosure timing with you
- **Credit**: If you wish, you will be credited for the discovery in release notes

## Security Best Practices

### For Contributors

When contributing to JavaVacunas, follow these security guidelines:

#### Authentication and Authorization

- Never hardcode credentials, API keys, or secrets
- Use environment variables for sensitive configuration
- Implement proper role-based access control (RBAC)
- Validate JWT tokens on every protected endpoint
- Use secure password hashing (BCrypt with appropriate cost factor)
- Implement proper session management

#### Input Validation

- Validate all user input on both frontend and backend
- Use parameterized queries to prevent SQL injection
- Sanitize input to prevent XSS attacks
- Implement proper content type validation
- Use Spring's `@Valid` annotation with appropriate constraints
- Never trust client-side validation alone

#### Data Protection

- Never log sensitive information (passwords, tokens, personal health data)
- Use HTTPS in production (TLS 1.2 or higher)
- Implement proper CORS configuration
- Encrypt sensitive data at rest
- Follow OWASP guidelines for secure data handling
- Comply with health data protection regulations (HIPAA, GDPR equivalents)

#### Dependencies

- Keep all dependencies up to date
- Run `mvn dependency-check:check` regularly for Java dependencies
- Run `npm audit` for Node.js dependencies
- Review security advisories for frameworks in use
- Use Dependabot or similar tools for automated updates

#### Database Security

- Use least privilege principle for database accounts
- Never expose database directly to the internet
- Implement proper connection pooling with timeouts
- Use prepared statements for all queries
- Regularly backup data with encryption
- Audit database access logs

#### API Security

- Implement rate limiting to prevent abuse
- Use appropriate HTTP methods and status codes
- Validate Content-Type headers
- Implement request size limits
- Log security-relevant events
- Use CSRF protection where applicable

### For Deployers

If you are deploying JavaVacunas:

#### Infrastructure

- Use containers with minimal base images
- Run containers as non-root users
- Keep host systems and container images updated
- Use secrets management tools (Vault, Kubernetes Secrets, etc.)
- Implement network segmentation
- Use firewalls to restrict access

#### Configuration

- Change default passwords immediately
- Use strong, unique passwords for all accounts
- Disable unnecessary services and endpoints
- Configure proper logging and monitoring
- Implement backup and disaster recovery procedures
- Use HTTPS with valid certificates

#### Monitoring

- Monitor for suspicious activity
- Set up alerts for security events
- Review logs regularly
- Implement intrusion detection systems
- Monitor for unauthorized access attempts

## Known Security Limitations (Alpha)

As an alpha release, JavaVacunas has known limitations:

- Limited security hardening for production environments
- Default credentials in seed data (must be changed in production)
- No comprehensive security audit has been performed
- No rate limiting implemented yet
- Limited input validation on frontend forms
- No account lockout mechanism for failed login attempts

**Do not use this alpha version in production healthcare environments without proper security review and hardening.**

## Security-Related Configuration

### JWT Token Security

- Default token expiration: 24 hours
- Tokens are signed with HMAC-SHA
- Store JWT secret in environment variables, never in code
- Rotate JWT secrets periodically in production

### Password Policy

- Minimum length enforced by application
- BCrypt hashing with cost factor 10
- Consider implementing password complexity requirements for production
- Implement password change and reset functionality before production use

### CORS Configuration

Current CORS allows:
- localhost:5173 (Vite dev server)
- localhost:3000 (alternative dev port)

**Change CORS configuration for production to allow only your production domains.**

## Compliance Considerations

JavaVacunas handles sensitive health data. When deploying:

- Ensure compliance with local health data protection laws in Paraguay
- Implement audit trails for data access and modifications
- Consider data retention and deletion policies
- Implement proper consent management
- Ensure data minimization principles
- Document data processing activities

## Security Updates

Security patches will be released as soon as possible after vulnerabilities are confirmed. Monitor:

- GitHub Security Advisories
- Release notes for security-related changes
- Dependency update notifications

## Contact

For security concerns that do not constitute vulnerabilities (questions about security features, best practices, etc.), you can:

- Open a regular GitHub issue labeled "security"
- Refer to the CONTRIBUTING.md guide
- Review the documentation

## Acknowledgments

We appreciate security researchers and contributors who help keep JavaVacunas secure. Responsible disclosure helps protect all users of the system.

Thank you for helping keep JavaVacunas and its users safe.
