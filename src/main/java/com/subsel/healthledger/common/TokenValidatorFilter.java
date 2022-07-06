//package com.priyo.priyomoney.wallet.common;
//
//import com.priyo.priyomoney.wallet.user.model.Agent;
//import com.priyo.priyomoney.wallet.user.repository.AgentRepository;
//import com.priyo.priyomoney.wallet.util.JsonConverter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Objects;
//import java.util.UUID;
//
//@Component
//public class TokenValidatorFilter extends OncePerRequestFilter {
//
//    private static final Logger log = LoggerFactory.getLogger(TokenValidatorFilter.class);
//
//    @Autowired
//    AgentRepository agentRepository;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        log.info("Inside the Token Validator filter: " + request.getRequestURI());
//
//        String tokenInfo = request.getHeader("Authorization");
//
//        if (Objects.nonNull(tokenInfo)) {
//
//            String uuid = JsonConverter.getUuidFromToken(request.getHeader("Authorization"));
//
//            if (tokenInfo.length() <= 6) {
//                response.sendError(400, "Invalid Authorization Header");
//            }
//
//            if (uuid.equals("")) {
//                response.sendError(400, "Token does not have uuid");
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}