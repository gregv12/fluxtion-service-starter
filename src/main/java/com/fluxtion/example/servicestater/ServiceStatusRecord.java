/*
 * Copyright (c) Greg Higgins 2021.
 *
 * Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.gnu.org/licenses/agpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.fluxtion.example.servicestater;

import lombok.Value;

/**
 * Immutable datatype representing the status of a service.
 */
@Value
public class ServiceStatusRecord {
    String serviceName;
    Service.Status status;

    @Override
    public String toString() {
        return "(service='" + serviceName + '\'' +
                ", status=" + status
                + ")";
    }
}
