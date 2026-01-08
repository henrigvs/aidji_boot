/*
 * Copyright 2025 Henri GEVENOIS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.aidji.boot.security.helpers;

import io.jsonwebtoken.Claims;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class SecurityHelper {

    public static List<String> extractAuthorities(Claims claims) {
        Object auth = claims.get("authorities");
        if (auth instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    // Dans SecurityHelper.java
    public static Map<String, Object> extractAdditionalClaims(Claims claims) {
        Set<String> standardClaims = Set.of(
                Claims.SUBJECT, Claims.ISSUER, Claims.ISSUED_AT,
                Claims.EXPIRATION, Claims.ID, Claims.NOT_BEFORE,
                Claims.AUDIENCE, "authorities"
        );

        return claims.entrySet().stream()
                .filter(entry -> !standardClaims.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
