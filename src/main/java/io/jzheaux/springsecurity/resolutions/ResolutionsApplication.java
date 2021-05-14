package io.jzheaux.springsecurity.resolutions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServletBearerExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication
public class ResolutionsApplication extends WebSecurityConfigurerAdapter {
    @Autowired
    UserRepositoryJwtAuthenticationConverter authenticationConverter;

    public static void main(String[] args) {
        SpringApplication.run(ResolutionsApplication.class, args);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests(authz -> authz.anyRequest()
                .authenticated())
                .httpBasic(basic -> {
                })
                .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken())                            //jwt().jwtAuthenticationConverter(this.authenticationConverter))//add UserRepositoryJwtAuthenticationConverter directly in DSL here, since it is a concrete impl instead of an interface you can't create a bean and have it picked up automatically
                .cors(cors -> {//somehow this configures spring security to allow CORS handshake
                });
//        http.csrf().disable();// todo
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository users) {
        return new UserRepositoryUserDetailsService(users);
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4000")
                        .allowedMethods("HEAD")
                        .allowedHeaders("Authorization");
            }
        };
    }
//    @Bean     //this got replaced with UserRepositoryJwtAuthenticationConverter
//    JwtAuthenticationConverter jwtAuthenticationConverter(){
//        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
//        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
//        authoritiesConverter.setAuthorityPrefix("");
//        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
//        return authenticationConverter;
//    }
    @Bean
    public OpaqueTokenIntrospector introspector(
            UserRepository userRepository,
            OAuth2ResourceServerProperties oAuth2ResourceServerProperties){
        OAuth2ResourceServerProperties.Opaquetoken ot = oAuth2ResourceServerProperties.getOpaquetoken();
        OpaqueTokenIntrospector introspector = new NimbusOpaqueTokenIntrospector(
                oAuth2ResourceServerProperties.getOpaquetoken().getIntrospectionUri(),
                oAuth2ResourceServerProperties.getOpaquetoken().getClientId(),
                oAuth2ResourceServerProperties.getOpaquetoken().getClientSecret()
        );
        return new UserRepositoryOpaqueTokenIntrospector(userRepository, introspector);
    }

    @Bean
    public WebClient.Builder web(){
        return WebClient.builder()
                .baseUrl("http://localhost:8081")//does every call on the builder return a new builder?
                .filter(new ServletBearerExchangeFilterFunction()); //now for each request made with this web client, Spring Security will look up the Bearer Token in the current Security Context and pass it along
    }
}
