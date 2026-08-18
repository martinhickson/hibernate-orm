package org.hibernate.orm.test.annotations.fetchprofile;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.Setting;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hibernate.Hibernate.isInitialized;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SessionFactory
@ServiceRegistry(settings = @Setting(name = AvailableSettings.USE_EAGER_TO_ONE_JOIN_FETCH, value = "false"))
@DomainModel(annotatedClasses = {EagerToOneJoinFetchDisabledTest.Parent.class, EagerToOneJoinFetchDisabledTest.Child.class})
public class EagerToOneJoinFetchDisabledTest {

	@Test
	void testSecondarySelectWhenDisabled(SessionFactoryScope scope) {
		scope.getCollectingStatementInspector().clear();
		scope.inTransaction( session -> {
			final Parent parent = new Parent();
			final Child child = new Child();
			child.parent = parent;
			session.persist( parent );
			session.persist( child );
		} );
		scope.getCollectingStatementInspector().clear();

		final List<Child> children = scope.fromSession( session ->
				session.createSelectionQuery( "from Child", Child.class ).getResultList()
		);
		assertTrue( isInitialized( children.get( 0 ).parent ) );
		scope.getCollectingStatementInspector().assertExecutedCount( 2 );
		scope.getCollectingStatementInspector().assertNumberOfJoins( 0, 0 );
		scope.getCollectingStatementInspector().assertNumberOfJoins( 1, 0 );
	}

	@Entity(name = "Parent")
	static class Parent {
		@Id
		@GeneratedValue
		Long id;
	}

	@Entity(name = "Child")
	static class Child {
		@Id
		@GeneratedValue
		Long id;
		@ManyToOne
		Parent parent;
	}
}
