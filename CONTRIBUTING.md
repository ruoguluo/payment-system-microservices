# Contributing to Payment System

We love your input! We want to make contributing to this project as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features
- Becoming a maintainer

## Development Process

We use GitHub to host code, to track issues and feature requests, as well as accept pull requests.

## Pull Requests

Pull requests are the best way to propose changes to the codebase. We actively welcome your pull requests:

1. Fork the repo and create your branch from `main`.
2. If you've added code that should be tested, add tests.
3. If you've changed APIs, update the documentation.
4. Ensure the test suite passes.
5. Make sure your code lints.
6. Issue that pull request!

## Development Setup

1. **Prerequisites**:
   - Java 17+
   - Maven 3.6+
   - Docker & Docker Compose
   - Stripe account (for testing)

2. **Local Development**:
   ```bash
   # Clone your fork
   git clone https://github.com/yourusername/payment-system-microservices.git
   cd payment-system-microservices
   
   # Set up environment
   cp .env.template .env
   # Edit .env with your Stripe test keys
   
   # Run the system
   ./deploy.sh
   ```

3. **Running Tests**:
   ```bash
   # Payment Service tests
   cd payment-service
   mvn test
   
   # Merchant Service tests
   cd merchant-service
   mvn test
   ```

## Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Add comments for complex business logic
- Keep methods small and focused
- Use proper exception handling

## Database Changes

If you need to modify the database schema:

1. Update the `docker-compose/init-db.sql` file
2. Create a migration script if needed
3. Update the JPA entities accordingly
4. Test with a fresh database deployment

## API Changes

When modifying APIs:

1. Update the DTOs and controllers
2. Update the README.md with new endpoints
3. Consider backward compatibility
4. Add appropriate validation

## Security Considerations

- Never commit API keys or secrets
- Use environment variables for configuration
- Validate all inputs
- Follow OWASP guidelines
- Consider rate limiting for new endpoints

## Testing Guidelines

- Write unit tests for new functionality
- Include integration tests for API endpoints
- Test error scenarios
- Mock external services (Stripe) in tests

## Commit Message Guidelines

Use clear and meaningful commit messages:

```
[SERVICE] Brief description of change

Longer explanation if necessary. Explain what and why,
not how (the code shows how).

- Changes made
- Why changes were necessary
- Any breaking changes
```

Examples:
- `[PAYMENT] Add webhook signature verification`
- `[MERCHANT] Implement API key rotation endpoint`
- `[DOCS] Update deployment instructions`

## Issue and Bug Reports

Use GitHub issues to track bugs and feature requests. Please use the issue templates provided.

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

## Questions?

Feel free to open an issue for any questions about contributing!
