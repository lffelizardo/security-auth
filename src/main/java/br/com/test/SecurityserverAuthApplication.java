package br.com.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@SpringBootApplication
@ComponentScan(basePackages="br.com.test")
@EnableJpaRepositories
@EntityScan(basePackages={ "br.com.test.domain" })
@EnableZuulProxy
public class SecurityserverAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityserverAuthApplication.class, args);
	}

	@Configuration
	protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

		@Override
		public void configure(HttpSecurity http) throws Exception {
			http.anonymous().and().authorizeRequests()
					.antMatchers("/health").permitAll()
					.antMatchers("/third-party/**").permitAll()
					.antMatchers("/saml/**").permitAll()
					.antMatchers("/samlmc/**").permitAll()
					.antMatchers("/awlegacy/sso").permitAll()
					.antMatchers("/awmock/**").permitAll()
					.antMatchers("/**").authenticated();
		}

	}

}