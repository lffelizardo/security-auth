package br.com.test.config;

import br.com.test.config.authentication.CustomUserDetails;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.util.Map;

import static br.com.test.config.OAuth2ServerConfiguration.ResourceServerConfiguration.RESOURCE_ID;

@Configuration
public class OAuth2ServerConfiguration {

    @Bean
    public static TokenStore tokenStore(){
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public static  JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyPair keyPair = new KeyStoreKeyFactory(
                new ClassPathResource("register.jks"),
                "123456".toCharArray()
        ).getKeyPair("ssl");
        converter.setKeyPair(keyPair);
        return converter;
    }

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        public static final String RESOURCE_ID = "restService";

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.tokenStore(tokenStore()).resourceId(RESOURCE_ID);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers("/oauth/**").permitAll()
                    .and()
                    .csrf().disable();
        }

    }

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends  AuthorizationServerConfigurerAdapter {

        @Autowired
        @Qualifier("authenticationManagerBean")
        private AuthenticationManager authenticationManager;

        @Value("${authServer.security.access.token.expiration.in.seconds}")
        private Integer accessTokenExpiration;

        @Value("${authServer.security.refresh.token.expiration.in.seconds}")
        private Integer refreshTokenExpiration;

        @Value("${authServer.security.server.token.expiration.in.seconds}")
        private Integer serverAccessTokenExpiration;

        @Value("${oauth2.clientapp.secret}")
        private String clientAppSecret;

        @Value("${oauth2.serverapp.secret}")
        private String serverAppSecret;

        @Autowired
        private UserDetailsService userDetailsService;

        @Autowired
        private Environment environment;

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) {

            endpoints
                    .authenticationManager(this.authenticationManager)
                    .tokenStore(OAuth2ServerConfiguration.tokenStore())
                    .accessTokenConverter(OAuth2ServerConfiguration.jwtAccessTokenConverter())
                    .approvalStoreDisabled();
        }

        private TokenEnhancer tokenEnhancer() {
            return (oAuth2AccessToken, oAuth2Authentication) -> {
                CustomUserDetails userDetails = (CustomUserDetails) oAuth2Authentication.getUserAuthentication().getPrincipal();
                Map<String, Object> additionalInfo = new HashedMap();
                additionalInfo.put("name", userDetails.getName());
                additionalInfo.put("emailAddress", userDetails.getEmailAddress());
                additionalInfo.put("username", userDetails.getUsername());
                ((DefaultOAuth2AccessToken) oAuth2AccessToken).setAdditionalInformation(additionalInfo);
                return oAuth2AccessToken;
            };
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.inMemory()
                    .withClient("clientapp")
                    .authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit")
                    .authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT")
                    .scopes("read", "write", "trust")
                    .resourceIds(RESOURCE_ID)
                    .secret(clientAppSecret)
                    .accessTokenValiditySeconds(accessTokenExpiration)
                    .refreshTokenValiditySeconds(refreshTokenExpiration)
             .and()
                    .withClient("serverapp")
                    .authorizedGrantTypes("password", "authorization_code")
                    .authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT")
                    .scopes("read", "write", "trust")
                    .resourceIds(RESOURCE_ID)
                    .secret(serverAppSecret)
                    .accessTokenValiditySeconds(serverAccessTokenExpiration);
        }

        @Bean
        @Primary
        public DefaultTokenServices tokenServices() {
            DefaultTokenServices tokenServices = new DefaultTokenServices();
            tokenServices.setSupportRefreshToken(true);
            tokenServices.setTokenStore(OAuth2ServerConfiguration.tokenStore());
            return tokenServices;
        }

    }

}