#!/bin/sh

#
# Copyright (c) 2022 StarTree Inc. All rights reserved.
# Confidential and Proprietary Information of StarTree Inc.
#

cat > ~/.npmrc <<EOF
; this will be our temp .npmrc credentials for Artifactory
@startree-ui:registry=https://repo.startreedata.io/artifactory/api/npm/startree-ui/
//repo.startreedata.io/artifactory/api/npm/startree-ui/:_password=QVA3cHphSlFzYnlMdUhXSmlOcGFtVEtub1ZmMzFVMkd3TDFpZkc=
//repo.startreedata.io/artifactory/api/npm/startree-ui/:username=chirag.m@deuexsolutions.com
//repo.startreedata.io/artifactory/api/npm/startree-ui/:email=chirag.m@deuexsolutions.com
//repo.startreedata.io/artifactory/api/npm/startree-ui/:always-auth=true
EOF

if [ $? -ne 0 ]; then
    exit 1
fi


cd src/thirdeye-ui && \
  npm install && \
  npm run release
