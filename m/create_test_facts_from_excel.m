function r = create_test_facts_from_excel(r,filename,sheet)
% load_rules_from_excel('C:\Documents and Settings\Dani\My Documents\PhD\research\projects\Rule-based System Architecting\Decadal Objective Rule Definition.xlsx','Weather');
[num,txt]= xlsread(filename,sheet);

call = '(deffacts testfacts  ';
space = ' ';
open_par = '(';
close_par = ')';
dash = '-';
% curr_subobj = '';
% curr_obj = '';
% nobj = 0;
% i = 2;
for i = 2:size(txt,1)
    line = txt(i,:);
%     obj = line{1};

%     subobj = line{3};

    type = line{6};
%     value = num(i-1);

%     desc = line{8};
    param = line{9};
    if(strcmp(type,'nominal'))
        
        call = [call '(Measurement ' open_par 'Parameter ' param close_par space];% (defrule subobjective-WE1-1-full "Conditions for full satisfaction of subobjective WE1-1" (Measurement (Parameter "1.4.1 atmospheric wind speed")
    %     curr_subobj = subobj;
        more_attributes = true;
        j = 10;
        while more_attributes
            attrib = line{j};
            [header,remain] = strtok(attrib,' ');
            if(strcmp(attrib,''))
                call = [call close_par];
                more_attributes = false;
            elseif(strncmp(header,'SameOrBetter',12))
                [att,val] = strtok(remain,' ');
%                 new_var_name = ['?x' num2str(ntests)];%?hsr&:(neq ?hsr nil)
%                 tmp = [att space new_var_name '&:(neq ' new_var_name ' nil)'];
                add_to_call = [open_par att val close_par];
                call = [call space add_to_call];
            elseif(strncmp(header,'ContainsRegion',14))
                [att,val] = strtok(remain,' ');
%                 new_var_name = ['?x' num2str(ntests)];%?hsr&:(neq ?hsr nil)
%                 tmp = [att space new_var_name '&:(neq ' new_var_name ' nil)'];
                add_to_call = [open_par att val close_par];
                call = [call space add_to_call];
                
            else
                add_to_call = [open_par attrib close_par];
                call = [call space add_to_call];
            end
            j = j + 1;
        end
    end
%     save call call;

%     i = i + 1;% new rule
end
call = [call ')'];
r.eval(call);

return
