/*
 * Copyright 2009-2014, Acciente LLC
 *
 * Acciente LLC licenses this file to you under the
 * Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.acciente.rsf.sql.internal.persister;

import com.acciente.rsf.AccessControlException;
import com.acciente.rsf.DomainCreatePermission;
import com.acciente.rsf.Resource;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class GrantDomainCreatePermissionSysPersister extends Persister {
   private final SQLStrings sqlStrings;

   public GrantDomainCreatePermissionSysPersister(SQLStrings sqlStrings) {
      this.sqlStrings = sqlStrings;
   }

   public Set<DomainCreatePermission> getDomainCreatePermissions(SQLConnection connection,
                                                                 Resource accessorResource) throws AccessControlException {
      SQLStatement statement = null;

      try {
         statement = connection.prepareStatement(sqlStrings.SQL_findInGrantDomainCreatePermissionSys_SysPermissionID_IsWithGrant_InheritLevel_BY_AccessorID);
         statement.setResourceId(1, accessorResource);
         SQLResult resultSet = statement.executeQuery();

         // first collect the create permissions that this resource has to resource domains
         Set<DomainCreatePermission> domainCreatePermissions = new HashSet<>();
         while (resultSet.next()) {
            domainCreatePermissions
                  .add(DomainCreatePermission.getInstance(resultSet.getDomainCreateSysPermissionName("SysPermissionId"),
                                                          resultSet.getBoolean("IsWithGrant")));
         }
         resultSet.close();

         return domainCreatePermissions;
      }
      catch (SQLException e) {
         throw new AccessControlException(e);
      }
      finally {
         closeStatement(statement);
      }
   }

   public void addDomainCreatePermissions(SQLConnection connection,
                                          Resource accessorResource,
                                          Resource grantorResource,
                                          Set<DomainCreatePermission> domainCreatePermissions) throws AccessControlException {
      SQLStatement statement = null;

      try {
         statement = connection.prepareStatement(sqlStrings.SQL_createInGrantDomainCreatePermissionSys_WITH_AccessorID_GrantorID_IsWithGrant_SysPermissionID);
         for (DomainCreatePermission domainCreatePermission : domainCreatePermissions) {
            if (domainCreatePermission.isSystemPermission()) {
               statement.setResourceId(1, accessorResource);
               statement.setResourceId(2, grantorResource);
               statement.setBoolean(3, domainCreatePermission.isWithGrant());
               statement.setDomainCreateSystemPermissionId(4, domainCreatePermission.getSystemPermissionId());

               assertOneRowInserted(statement.executeUpdate());
            }
         }
      }
      catch (SQLException e) {
         throw new AccessControlException(e);
      }
      finally {
         closeStatement(statement);
      }
   }

   public void removeDomainCreatePermissions(SQLConnection connection,
                                             Resource accessorResource) throws AccessControlException {
      SQLStatement statement = null;

      try {
         statement = connection.prepareStatement(sqlStrings.SQL_removeInGrantDomainCreatePermissionSys_BY_AccessorID);
         statement.setResourceId(1, accessorResource);
         statement.executeUpdate();
      }
      catch (SQLException e) {
         throw new AccessControlException(e);
      }
      finally {
         closeStatement(statement);
      }
   }
}
