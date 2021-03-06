function SMAP_precompute_revtimes()
%% EOCubesats_precompute_revtimes.m
%
% This function computes the scores of all subsets of instrument in all
% possible orbits
% 
% revtimes.get([FOV, orbits]) = [revtime]
global params revtimes
revtimes = java.util.HashMap;
orbs = params.orbit_list;norb = length(orbs);
% instr = params.instrument_list;
jess reset;
[~,v] = get_all_data('DATABASE::Instrument',{'Field-of-view#'},{'single-char'},0);
params.fovs = cellfun(@char,depack_cellofcells(v),'UniformOutput',false);
uFOVS = unique(params.fovs);
nfovs = length(uFOVS);
% thefovs = [-1 params.fovs];

%% Enumerate adjacency matrices FOV-orbit

stk_params = init_STK();
sum = 0;
archs = npermutek(0:nfovs,norb);
narch = size(archs,1);
for i=1:narch
    arch = archs(i,:);
    fovs = get_fovs(arch,uFOVS);% numeric fov array with -1 if empty orbit
    fprintf('Precomputing arch %d from %d...\n',i,narch);tic;
    therevtimes = analyze_mixed_constellation(fovs,stk_params);
    revtimes = put_revtimes(revtimes,fovs,therevtimes);
    t = toc;
    sum = sum + t/60;
    done = i;
    all = narch;
    remaining = all - done;
    fprintf('%.1f min elapsed total (%.1f sec for this iter). %.1f min remaining\n',sum,t,remaining*t/60);
end
save revtimes revtimes
end

function revtimes = put_revtimes(revtimes,fovs,therevtimes)
%    key = get_key2(fovs,orbits);
   revtimes.put(int2str(fovs),therevtimes);
end

function fovs = get_fovs(arch,allfovs)
    fovs = zeros(size(arch));
    for i = 1:length(fovs)
        if arch(i) == 0
            fovs(i) = -1;
        else
            fovs(i) = str2double(allfovs(arch(i)));
        end
    end
end

function params = init_STK()
    params.DURATION = 7*24*60*60;% simulate for 7 days (in seconds)
    params.TSTEP = 60;% time step is set to 1 minute, this gives us about 95 points per orbit and 20160 points per simulation
    params.path= '.\';
    params.path_save_results = params.path;
    params.LAT_LON_GRANULARITY = 6;
    params.delta_raan = 0;
    params.MAX_LAT = 90;
    params.e = 0;
    params.arg_perigee = 0;
    remMachine = stkDefaultHost;
    params.conid = stkOpen(remMachine);% params structure will be use to call internal function
    scenario_name = 'EOCubsats2';
    stkNewObj('/','Scenario',scenario_name);% creates scenario
    params.scenario_path = ['/Scenario/' scenario_name '/'];% will use this later
    stkSetTimePeriodInSec(0, params.DURATION)
    stkSetEpoch('2013001.000000', 'YYYYDDD.HHMMSS');
    stkSetTimePeriodInSec(0, params.DURATION)
    call = ['SetAnimation ' params.scenario_path ' StartAndCurrentTime UseAnalysisStartTime TimeStep ' num2str(params.TSTEP)];
    stkExec(params.conid,call);

end

function revtimes = analyze_mixed_constellation(fovs,stk_params)
global params
    % Coverage grid
    coverage_name = 'GlobalCoverage';
    stkNewObj(stk_params.scenario_path, 'CoverageDefinition', coverage_name);% creates coverage definition
    coverage_path = [stk_params.scenario_path 'CoverageDefinition/' coverage_name '/'];
    stkSetCoverageBounds(stk_params.conid, [stk_params.scenario_path 'CoverageDefinition/' coverage_name], -stk_params.MAX_LAT, +stk_params.MAX_LAT);% sets max/min latitude
    call = ['Cov ' coverage_path ' Grid PointGranularity LatLon ' num2str(stk_params.LAT_LON_GRANULARITY)];
    stkExec(stk_params.conid, call);

    % Create Figure of Merit: Average Revisit Time
    FOM_name2 = 'Avg_Revisit_Time';
    stkNewObj(coverage_path, 'FigureOfMerit', FOM_name2);
    FOM_path2 = [coverage_path 'FigureOfMerit/' FOM_name2 '/'];
    call = ['FOMDefine ' FOM_path2 ' ' 'Definition RevisitTime Compute Average'];
    stkExec(stk_params.conid, call);

    stkNewObj(stk_params.scenario_path, 'Constellation', 'MyCons');
    constel_path = [stk_params.scenario_path 'Constellation/' 'MyCons'];

    tStart  = 0;
    tStop   = stk_params.DURATION;
    tStep   = stk_params.TSTEP;
    orbits = params.orbit_list;
    % Add instrument with the right fov to all orbits
    norb = length(fovs);
    for i = 1:norb
        fov = fovs(i);
        if fov ~= -1
            orbit = get_orbit_struct_from_string(orbits{i});
            semimajorAxis = (6378 + str2num(orbit.altitude))*1000;
            [~,i1] = get_orbit_inclination(orbits{i});
            inc = i1*pi/180;
            [~,raan] = get_orbit_raan(orbits{i});
            sat_name = orbits{i};
            stkNewObj(stk_params.scenario_path, 'Satellite', sat_name);
            sat_path = [stk_params.scenario_path 'Satellite/' sat_name];
            anomaly = (i-1)*2*pi/norb;% 180/#planes separation between planes
            stkSetPropClassical(sat_path, 'J2Perturbation','J2000',tStart,tStop,tStep,0,semimajorAxis,stk_params.e,inc,stk_params.arg_perigee,raan,anomaly);
%             sat_path = [stk_params.scenario_path 'Satellite/' sat_name];


            sensor_name = num2str(fov);

            stkNewObj(sat_path, 'Sensor', sensor_name);
            sensor_path = [sat_path '/Sensor/' sensor_name]; 
            % set the satellite sensor
            stkSetSensor(stk_params.conid, sensor_path,'Rectangular', fov,fov);

            call = ['Chains ' constel_path ' Add ' sensor_path];  
            stkExec(stk_params.conid, call);
        end
    end

    % Assign coverage grid as asset to constellation
    stkSetCoverageAsset(stk_params.conid, coverage_path, constel_path);
    
    call = ['CovAccess ' coverage_path ' Compute ' ''];
    stkExec(stk_params.conid, call);
    
    % CDF Revisit time
    [rt_data, ~] = stkReport(coverage_path, 'Gap Duration');
    cdf.over = stkFindData(rt_data{2},'% Over');
    cdf.under = stkFindData(rt_data{2},'% Under');
    cdf.duration = stkFindData(rt_data{2},'Duration');

    % Mean revisit time
    [rt_data, ~] = stkReport(FOM_path2, 'Value By Grid Point');
    rt_value2 = stkFindData(rt_data{3}, 'FOM Value');    % revisit time by grid point
    rts = rt_value2./3600;
    
    % Lat and lon
    lat = stkFindData(rt_data{3}, 'Latitude')*180/pi;
    lon = stkFindData(rt_data{3}, 'Longitude')*180/pi - 180;
    
    times = cdf.duration;
    pctgs = cdf.under;
    [~,index_median] = min(abs(pctgs-50));
    median = times(index_median);
    pctgs = pctgs./100;
    mean = 0;
    old = 0;
    for j = 1:length(pctgs)
        mean = mean + (pctgs(j)-old)*times(j);
        old = pctgs(j);
    end
    avg_rev_time_global = mean/3600;
    revtimes = [avg_rev_time_global];
    
    
    
    % %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    % Clean STK for next architecture
    % %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    objects = stkObjNames;
    stkUnload(objects{3});
    objects = stkObjNames;

    for i = length(objects):-1:2
        stkUnload(objects{i});
    end
end