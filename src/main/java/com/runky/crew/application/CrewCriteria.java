package com.runky.crew.application;

import com.runky.crew.domain.CrewCommand;

public class CrewCriteria {
    public record Create(
            Long userId,
            String name
    ) {
        public CrewCommand.Create toCommand() {
            return new CrewCommand.Create(
                    userId,
                    name
            );
        }
    }

    public record Join(
            Long userId,
            String code
    ) {
        public CrewCommand.Join toCommand() {
            return new CrewCommand.Join(
                    userId,
                    code
            );
        }
    }

    public record Card(
            Long userId
    ) {
    }

    public record Detail(
            Long crewId,
            Long userId
    ) {
        public CrewCommand.Detail toCrewCommand() {
            return new CrewCommand.Detail(
                    crewId,
                    userId
            );
        }
    }

    public record Leave(
            Long crewId,
            Long userId,
            Long newLeaderId
    ) {
        public CrewCommand.Leave toCommand() {
            return new CrewCommand.Leave(
                    crewId,
                    userId,
                    newLeaderId
            );
        }
    }

    public record Members(
            Long crewId,
            Long userId
    ) {
        public CrewCommand.Members toCommand() {
            return new CrewCommand.Members(
                    crewId,
                    userId
            );
        }
    }

    public record UpdateNotice(
            Long crewId,
            Long userId,
            String notice
    ) {
        public CrewCommand.UpdateNotice toCommand() {
            return new CrewCommand.UpdateNotice(
                    crewId,
                    userId,
                    notice
            );
        }
    }

    public record UpdateName(
            Long crewId,
            Long userId,
            String notice
    ) {
        public CrewCommand.UpdateName toCommand() {
            return new CrewCommand.UpdateName(
                    crewId,
                    userId,
                    notice
            );
        }
    }

    public record Disband(
            Long crewId,
            Long userId
    ) {
        public CrewCommand.Disband toCommand() {
            return new CrewCommand.Disband(
                    crewId,
                    userId
            );
        }
    }

    public record Delegate(
            Long crewId,
            Long userId,
            Long newLeaderId
    ) {
        public CrewCommand.Delegate toCommand() {
            return new CrewCommand.Delegate(
                    crewId,
                    userId,
                    newLeaderId
            );
        }
    }

    public record Ban(
            Long crewId,
            Long userId,
            Long banMemberId
    ) {
        public CrewCommand.Ban toCommand() {
            return new CrewCommand.Ban(
                    crewId,
                    userId,
                    banMemberId
            );
        }
    }

    public record RelatedRunningMember(
            Long userId
    ) {
    }

    private CrewCriteria() {
    }
}
