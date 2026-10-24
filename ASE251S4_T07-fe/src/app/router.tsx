import React from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { Layout } from '../shared/components/Layout';
import { LoginPage } from '../features/auth/pages/LoginPage';
import { DashboardPage } from '../features/dashboard/pages/DashboardPage';
import { ProductsPage } from '../features/products/pages/ProductsPage';
import { CustomersPage } from '../features/customers/pages/CustomersPage';
import { SuppliesPage } from '../features/supplies/pages/SuppliesPage';
import { SalesOrdersPage } from '../features/sales/pages/SalesOrdersPage';
import { CollectionsPage } from '../features/collections/pages/CollectionsPage';
import { MeetingsPage } from '../features/meetings/pages/MeetingsPage';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        index: true,
        element: <Navigate to="/dashboard" replace />,
      },
      {
        path: 'dashboard',
        element: <DashboardPage />,
      },
      {
        path: 'products',
        element: <ProductsPage />,
      },
      {
        path: 'customers',
        element: <CustomersPage />,
      },
      {
        path: 'supplies',
        element: <SuppliesPage />,
      },
      {
        path: 'sales',
        element: <SalesOrdersPage />,
      },
      {
        path: 'collections',
        element: <CollectionsPage />,
      },
      {
        path: 'meetings',
        element: <MeetingsPage />,
      },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/dashboard" replace />,
  },
]);
